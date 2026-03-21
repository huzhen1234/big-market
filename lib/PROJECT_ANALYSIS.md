# 大营销项目 DDD架构深度分析报告

## 一、项目整体结构与模块职责

### 1.1 模块依赖关系

```
big-market-app (应用启动层)
    ├── big-market-trigger (触发器层)
    │       ├── big-market-api (接口定义层)
    │       └── big-market-domain (领域层)
    │               └── big-market-types (通用类型层)
    └── big-market-infrastructure (基础设施层)
            └── big-market-domain (领域层)
```

### 1.2 各模块职责

| 模块 | 职责 | 关键内容 |
|------|------|----------|
| **big-market-api** | 接口定义层，定义对外暴露的服务接口 | `IRaffleActivityService`, `IRaffleStrategyService`, DTO对象 |
| **big-market-app** | 应用启动层，配置和组装 | 配置类、启动类、AOP切面 |
| **big-market-domain** | 核心领域层，业务逻辑 | 领域模型、领域服务、仓储接口、聚合根 |
| **big-market-infrastructure** | 基础设施层，技术实现 | 仓储实现、DAO、Redis、消息队列、网关 |
| **big-market-trigger** | 触发器层，入口 | HTTP Controller、MQ消费者、定时任务、RPC服务 |
| **big-market-types** | 通用类型定义 | 常量、枚举、异常、注解、事件基类 |

---

## 二、核心业务领域分析

### 2.1 领域模型总览

项目识别出**6个核心领域上下文**：

```
big-market-domain/
├── activity/        # 活动领域 - 活动配置、SKU商品、账户额度管理
├── strategy/        # 策略领域 - 抽奖策略、规则引擎
├── award/           # 奖品领域 - 奖品发放
├── credit/          # 积分领域 - 积分账户、积分交易
├── rebate/          # 返利领域 - 行为返利
├── task/            # 任务领域 - 补偿任务
└── auth/            # 认证领域 - Token校验
```

### 2.2 主要业务线

#### 业务线1：抽奖活动（核心业务）

**涉及领域**：activity、strategy、award

**业务流程**：
```
用户请求参与活动 → 创建抽奖订单 → 执行抽奖策略 → 返回中奖结果 → 发放奖品
```

**核心功能**：
- 活动配置与管理
- 账户额度控制（总账户/月账户/日账户）
- 抽奖策略执行（责任链 + 决策树）
- 奖品发放（多种奖品类型）

#### 业务线2：积分系统

**涉及领域**：credit、activity

**业务流程**：
```
用户积分获取 → 积分账户管理 → 积分消费（兑换SKU商品） → 积分交易记录
```

**核心功能**：
- 积分账户管理
- 积分交易（正向增加、逆向扣减）
- 积分兑换商品
- 交易流水记录

#### 业务线3：行为返利

**涉及领域**：rebate、task

**业务流程**：
```
用户行为触发 → 查询返利配置 → 创建返利订单 → 发送MQ消息 → 消费处理返利
```

**核心功能**：
- 行为返利配置
- 返利订单创建
- MQ消息发送
- 补偿任务机制

### 2.3 核心聚合设计

#### 1. CreatePartakeOrderAggregate - 参与活动订单聚合

**路径**: `domain/activity/model/aggregate/CreatePartakeOrderAggregate.java`

```java
public class CreatePartakeOrderAggregate {
    private String userId;                              // 用户ID
    private Long activityId;                            // 活动ID
    private ActivityAccountEntity activityAccountEntity; // 总账户
    private ActivityAccountMonthEntity activityAccountMonthEntity; // 月账户
    private ActivityAccountDayEntity activityAccountDayEntity;     // 日账户
    private UserRaffleOrderEntity userRaffleOrderEntity; // 抽奖订单
}
```

**设计思想**：
- 将用户账户（总/月/日）与抽奖订单聚合为一个事务单元
- 保证账户扣减与订单创建的原子性
- 实现多层级账户额度控制

**为什么需要三层账户？**
- 总账户：用户总抽奖次数额度
- 月账户：按月限制抽奖次数
- 日账户：按日限制抽奖次数
- 防止用户过度抽奖，实现精细化运营控制

#### 2. TradeAggregate - 积分交易聚合

**路径**: `domain/credit/model/aggregate/TradeAggregate.java`

```java
public class TradeAggregate {
    private String userId;
    private CreditAccountEntity creditAccountEntity;  // 积分账户
    private CreditOrderEntity creditOrderEntity;      // 积分订单
    private TaskEntity taskEntity;                     // 补偿任务
}
```

**设计思想**：
- 将积分账户变动、订单创建、补偿任务绑定在一起
- 保证积分交易的一致性
- 通过TaskEntity实现最终一致性保障

#### 3. GiveOutPrizesAggregate - 发放奖品聚合

**路径**: `domain/award/model/aggregate/GiveOutPrizesAggregate.java`

```java
public class GiveOutPrizesAggregate {
    private String userId;
    private UserAwardRecordEntity userAwardRecordEntity; // 发奖记录
    private UserCreditAwardEntity userCreditAwardEntity; // 积分奖品
}
```

**设计思想**：
- 将发奖记录与具体奖品发放聚合
- 支持多种奖品类型（积分、优惠券、实物等）
- 保证发奖记录与奖品发放的一致性

#### 4. BehaviorRebateAggregate - 行为返利聚合

```java
public class BehaviorRebateAggregate {
    private String userId;
    private BehaviorRebateOrderEntity behaviorRebateOrderEntity; // 返利订单
    private TaskEntity taskEntity; // MQ消息任务
}
```

**设计思想**：
- 返利订单与MQ消息任务聚合
- 保证订单创建与消息发送的一致性
- 通过补偿机制确保消息最终送达

### 2.4 实体与值对象

#### 实体（Entity） - 具有唯一标识

**活动领域**：
- `ActivityEntity` - 活动实体
- `ActivitySkuEntity` - 活动SKU实体
- `UserRaffleOrderEntity` - 用户抽奖订单
- `ActivityAccountEntity` - 活动账户

**策略领域**：
- `StrategyEntity` - 策略实体
- `StrategyAwardEntity` - 策略奖品实体

**积分领域**：
- `CreditAccountEntity` - 积分账户
- `CreditOrderEntity` - 积分订单

#### 值对象（Value Object） - 无标识，不可变

**状态值对象**：
- `ActivityStateVO` - 活动状态（开放/关闭）
- `OrderStateVO` - 订单状态（待支付/完成）
- `TaskStateVO` - 任务状态（待发送/已发送/失败）

**规则值对象**：
- `RuleLogicCheckTypeVO` - 规则校验类型（放行/接管）
- `TreeActionEntity` - 决策树动作结果

**交易值对象**：
- `TradeTypeVO` - 交易类型（正向/逆向）
- `TradeNameVO` - 交易名称

**为什么使用值对象？**
- 封装业务规则和校验逻辑
- 提高代码可读性和类型安全性
- 避免基本类型泛滥（Primitive Obsession）
- 便于业务规则的集中管理

### 2.5 领域服务设计

| 领域 | 服务接口 | 实现类 | 职责 |
|------|----------|--------|------|
| Strategy | `IRaffleStrategy` | `DefaultRaffleStrategy` | 执行抽奖策略 |
| Activity | `IRaffleActivityPartakeService` | `RaffleActivityPartakeService` | 参与活动 |
| Activity | `IRaffleActivityAccountQuotaService` | `RaffleActivityAccountQuotaService` | 账户额度管理 |
| Award | `IAwardService` | `AwardService` | 奖品发放 |
| Credit | `ICreditAdjustService` | `CreditAdjustService` | 积分调额 |
| Rebate | `IBehaviorRebateService` | `BehaviorRebateService` | 行为返利 |

---

## 三、设计模式应用分析

### 3.1 模板方法模式（Template Method）

#### 应用场景1：抽奖策略执行流程

**路径**: `domain/strategy/service/AbstractRaffleStrategy.java`

```java
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {

    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {
        // 1. 参数校验
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        Date endDateTime = raffleFactorEntity.getEndDateTime();
        
        // 2. 责任链抽奖计算【模板方法，由子类实现】
        DefaultChainFactory.StrategyAwardVO chainStrategyAwardVO = raffleLogicChain(userId, strategyId);
        
        // 3. 规则树抽奖过滤【模板方法，由子类实现】
        DefaultTreeFactory.StrategyAwardVO treeStrategyAwardVO = raffleLogicTree(userId, strategyId, awardId, endDateTime);
        
        // 4. 返回抽奖结果
        return buildRaffleAwardEntity(strategyId, awardId, awardConfig);
    }

    // 抽象方法，由子类实现
    public abstract DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId);
    public abstract DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId, Date endDateTime);
}
```

**为什么选择模板方法？**
- ✅ 抽奖流程骨架固定：参数校验 → 责任链计算 → 决策树过滤 → 返回结果
- ✅ 具体规则处理可能变化（不同策略有不同的规则处理）
- ✅ 控制流程扩展点，子类只需关注具体实现
- ❌ 不选择策略模式：流程骨架本身不变，只是部分步骤实现不同
- ❌ 不选择继承：模板方法本质就是继承，但提供了更清晰的扩展点

#### 应用场景2：参与活动流程

**路径**: `domain/activity/service/partake/AbstractRaffleActivityPartake.java`

```java
public UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
    // 1. 活动查询
    ActivityEntity activityEntity = queryRaffleActivityByActivityId(activityId);
    
    // 2. 查询未被使用的活动参与订单记录
    UserRaffleOrderEntity order = queryNoUsedRaffleOrder(partakeRaffleActivityEntity);
    if (null != order) return order;
    
    // 3. 额度账户过滤【抽象方法】
    CreatePartakeOrderAggregate aggregate = this.doFilterAccount(userId, activityId, currentDate);
    
    // 4. 构建订单【抽象方法】
    UserRaffleOrderEntity userRaffleOrder = this.buildUserRaffleOrder(userId, activityId, currentDate);
    
    // 5. 保存聚合对象
    activityRepository.saveCreatePartakeOrderAggregate(aggregate);
    
    return userRaffleOrder;
}

// 子类实现不同的账户过滤逻辑
protected abstract CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate);
protected abstract UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date currentDate);
```

**设计优势**：
- 定义标准流程，保证业务一致性
- 子类只需实现差异化的逻辑
- 便于后续扩展新的活动类型

### 3.2 策略模式（Strategy）

#### 应用场景1：抽奖算法选择

**接口定义**：`domain/strategy/service/armory/algorithm/IAlgorithm.java`

```java
public interface IAlgorithm {
    void armoryAlgorithm(String key, List<StrategyAwardEntity> strategyAwardEntities, BigDecimal rateRange);
    Integer dispatchAlgorithm(String key);
}
```

**具体策略**：
- `O1Algorithm` - O(1)时间复杂度算法，适合概率范围小的情况
- `OLogNAlgorithm` - O(logN)时间复杂度算法，适合概率范围大的情况

**策略选择**：

```java
@Override
protected void armoryAlgorithm(String key, List<StrategyAwardEntity> strategyAwardEntities) {
    double rateRange = convert(BigDecimal.valueOf(minAwardRate.doubleValue()));
    
    // 根据概率范围自动选择算法
    if (rateRange <= ALGORITHM_THRESHOLD_VALUE) {
        IAlgorithm o1Algorithm = algorithmMap.get(AbstractAlgorithm.Algorithm.O1.getKey());
        o1Algorithm.armoryAlgorithm(key, strategyAwardEntities, new BigDecimal(rateRange));
    } else {
        IAlgorithm oLogNAlgorithm = algorithmMap.get(AbstractAlgorithm.Algorithm.OLogN.getKey());
        oLogNAlgorithm.armoryAlgorithm(key, strategyAwardEntities, new BigDecimal(rateRange));
    }
}
```

**为什么选择策略模式？**
- ✅ 抽奖算法可替换：根据场景选择不同算法
- ✅ 算法独立封装：每个算法独立演化
- ✅ 易于扩展：新增算法只需实现接口
- ❌ 不选择模板方法：算法之间没有共同流程，是完全独立的实现
- ❌ 不选择硬编码if-else：违反开闭原则，难以维护

#### 应用场景2：交易策略

**接口定义**：`domain/activity/service/quota/policy/ITradePolicy.java`

```java
public interface ITradePolicy {
    void trade(CreateQuotaOrderAggregate createQuotaOrderAggregate);
}
```

**具体策略**：
- `CreditPayTradePolicy` - 积分支付策略
- `RebateNoPayTradePolicy` - 返利无支付策略
- `CreditNoPayTradePolicy` - 积分无支付策略

**策略使用**：

```java
// 通过Map注入不同策略实现
private final Map<String, ITradePolicy> tradePolicyGroup;

// 根据交易类型选择策略
ITradePolicy tradePolicy = tradePolicyGroup.get(skuRechargeEntity.getOrderTradeType().getCode());
tradePolicy.trade(createOrderAggregate);
```

**设计优势**：
- 支付方式灵活切换
- 新增支付方式只需实现接口
- 通过Spring自动注入，配置化选择策略

#### 应用场景3：奖品发放策略

**接口定义**：`domain/award/service/distribute/IDistributeAward.java`

```java
public interface IDistributeAward {
    void giveOutPrizes(DistributeAwardEntity distributeAwardEntity) throws Exception;
}
```

**具体策略**：
- `UserCreditRandomAward` - 积分随机奖品
- `UserCreditSingleAward` - 积分固定奖品
- `OpenAIAccountAdjustQuotaAward` - OpenAI账户调额奖品
- `CouponAward` - 优惠券奖品

**设计优势**：
- 奖品类型可灵活扩展
- 每种奖品的发放逻辑独立
- 支持组合奖品发放

### 3.3 责任链模式（Chain of Responsibility）

#### 应用场景：抽奖规则前置过滤

**抽象处理者**：`domain/strategy/service/rule/chain/AbstractLogicChain.java`

```java
public abstract class AbstractLogicChain implements ILogicChain {
    private ILogicChain next;

    @Override
    public ILogicChain next() {
        return next;
    }

    @Override
    public ILogicChain appendNext(ILogicChain next) {
        this.next = next;
        return next;
    }

    protected abstract String ruleModel();
}
```

**具体处理者**：

```java
// 黑名单处理
@Component("rule_blacklist")
public class BlackListLogicChain extends AbstractLogicChain {
    @Override
    public DefaultChainFactory.StrategyAwardVO logic(String userId, Long strategyId) {
        // 黑名单判断逻辑
        for (String userBlackId : userBlackIds) {
            if (userId.equals(userBlackId)) {
                return DefaultChainFactory.StrategyAwardVO.builder()
                    .awardId(awardId)
                    .logicModel(ruleModel())
                    .build();
            }
        }
        // 传递给下一个处理者
        return next().logic(userId, strategyId);
    }
}

// 权重规则处理
@Component("rule_weight")
public class RuleWeightLogicChain extends AbstractLogicChain { ... }

// 默认处理
@Component("rule_default")
public class DefaultLogicChain extends AbstractLogicChain { ... }
```

**责任链组装**：

```java
// 路径: domain/strategy/service/rule/chain/factory/DefaultChainFactory.java
public ILogicChain openLogicChain(Long strategyId) {
    StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);
    String[] ruleModels = strategy.ruleModels();
    
    // 按配置顺序组装责任链
    ILogicChain logicChain = applicationContext.getBean(ruleModels[0], ILogicChain.class);
    ILogicChain current = logicChain;
    for (int i = 1; i < ruleModels.length; i++) {
        ILogicChain nextChain = applicationContext.getBean(ruleModels[i], ILogicChain.class);
        current = current.appendNext(nextChain);
    }
    // 最后添加默认责任链
    current.appendNext(applicationContext.getBean(LogicModel.RULE_DEFAULT.getCode(), ILogicChain.class));
    return logicChain;
}
```

**为什么选择责任链模式？**
- ✅ 规则顺序执行：黑名单 → 权重规则 → 默认抽奖
- ✅ 规则可配置：通过配置决定启用哪些规则
- ✅ 单一职责：每个规则处理器只关注自己的逻辑
- ✅ 易于扩展：新增规则只需添加新的处理器
- ❌ 不选择策略模式：规则之间有顺序依赖，需要依次判断
- ❌ 不选择if-else：代码臃肿，难以维护和扩展

**活动下单责任链**：

```java
// 路径: domain/activity/service/quota/rule/AbstractActionChain.java
public abstract class AbstractActionChain implements IActionChain {
    private IActionChain next;
    // 处理SKU库存扣减等活动校验
}
```

### 3.4 工厂模式（Factory）

#### 应用场景1：责任链工厂

**路径**: `domain/strategy/service/rule/chain/factory/DefaultChainFactory.java`

```java
@Service
public class DefaultChainFactory {
    private final Map<Long, ILogicChain> strategyChainGroup; // 缓存责任链
    
    public ILogicChain openLogicChain(Long strategyId) {
        // 根据策略ID构建对应的责任链
        if (strategyChainGroup.containsKey(strategyId)) {
            return strategyChainGroup.get(strategyId);
        }
        // 构建新的责任链并缓存
        ILogicChain logicChain = buildLogicChain(strategyId);
        strategyChainGroup.put(strategyId, logicChain);
        return logicChain;
    }
}
```

#### 应用场景2：决策树工厂

**路径**: `domain/strategy/service/rule/tree/factory/DefaultTreeFactory.java`

```java
@Service
public class DefaultTreeFactory {
    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;
    
    public IDecisionTreeEngine openLogicTree(RuleTreeVO ruleTreeVO) {
        return new DecisionTreeEngine(logicTreeNodeGroup, ruleTreeVO);
    }
}
```

**为什么选择工厂模式？**
- ✅ 封装复杂创建逻辑：责任链、决策树的构建过程复杂
- ✅ 统一管理实例：缓存已创建的实例，避免重复创建
- ✅ 隔离客户端与创建逻辑：客户端只需传入ID即可获得实例
- ❌ 不选择直接new：创建逻辑复杂，不适合分散在各处

### 3.5 组合模式（Composite）

#### 应用场景：决策树规则引擎

**决策树引擎**：`domain/strategy/service/rule/tree/factory/engine/impl/DecisionTreeEngine.java`

```java
public class DecisionTreeEngine implements IDecisionTreeEngine {

    @Override
    public DefaultTreeFactory.StrategyAwardVO process(String userId, Long strategyId, Integer awardId, Date endDateTime) {
        String nextNode = ruleTreeVO.getTreeRootRuleNode();
        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();
        
        // 遍历决策树节点
        while (null != nextNode) {
            ILogicTreeNode logicTreeNode = logicTreeNodeGroup.get(ruleTreeNode.getRuleKey());
            DefaultTreeFactory.TreeActionEntity logicEntity = logicTreeNode.logic(userId, strategyId, awardId, ruleValue, endDateTime);
            nextNode = nextNode(ruleLogicCheckTypeVO.getCode(), ruleTreeNode.getTreeNodeLineVOList());
        }
        return strategyAwardData;
    }
}
```

**决策树节点**：
- `RuleLockLogicTreeNode` - 次数锁节点
- `RuleStockLogicTreeNode` - 库存扣减节点
- `RuleLuckAwardLogicTreeNode` - 兜底奖品节点

**为什么选择组合模式？**
- ✅ 树形结构：决策规则天然形成树形结构
- ✅ 统一接口：所有节点实现相同的接口`ILogicTreeNode`
- ✅ 递归处理：引擎统一遍历节点，节点内部处理自己的逻辑
- ❌ 不选择策略模式：节点之间存在父子关系，需要树形组织
- ❌ 不选择责任链：决策树支持分支条件，不是简单的线性链

### 3.6 仓储模式（Repository）

#### 领域层定义仓储接口

**路径**: `domain/activity/adapter/repository/IActivityRepository.java`

```java
public interface IActivityRepository {
    ActivitySkuEntity queryActivitySku(Long sku);
    ActivityEntity queryRaffleActivityByActivityId(Long activityId);
    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate);
    // ... 其他方法
}
```

#### 基础设施层实现仓储

**路径**: `infrastructure/adapter/repository/ActivityRepository.java`

```java
@Repository
public class ActivityRepository implements IActivityRepository {
    @Resource private IRedisService redisService;
    @Resource private IRaffleActivityDao raffleActivityDao;
    @Resource private TransactionTemplate transactionTemplate;
    @Resource private IDBRouterStrategy dbRouter;
    
    @Override
    public void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate aggregate) {
        dbRouter.doRouter(userId); // 分库分表路由
        transactionTemplate.execute(status -> {
            // 更新总账户、月账户、日账户、写入订单
            raffleActivityAccountDao.updateActivityAccountSubtractionQuota(...);
            raffleActivityAccountMonthDao.updateActivityAccountMonthSubtractionQuota(...);
            raffleActivityAccountDayDao.updateActivityAccountDaySubtractionQuota(...);
            userRaffleOrderDao.insert(...);
            return 1;
        });
    }
}
```

**为什么选择仓储模式？**
- ✅ 领域层解耦：领域层不依赖具体的数据访问技术
- ✅ 聚合持久化：封装聚合对象的事务性保存
- ✅ 抽象数据访问：领域层通过接口操作数据，不关心底层实现
- ✅ 便于测试：可以轻松Mock仓储接口进行单元测试
- ❌ 不选择DAO：DAO只是数据访问对象，不承载领域概念
- ❌ 不选择Active Record：会导致领域模型贫血，违反DDD原则

---

## 四、关键业务流程分析

### 4.1 抽奖活动完整流程

**流程图**：

```
用户请求 → Controller → 参与活动创建订单 → 执行抽奖策略 → 保存中奖记录 → 发放奖品
```

**代码入口**：`trigger/http/RaffleActivityController.draw()`

```java
@RequestMapping(value = "draw", method = RequestMethod.POST)
public Response<ActivityDrawResponseDTO> draw(@RequestBody ActivityDrawRequestDTO request) {
    // 1. 参与活动 - 创建参与记录订单
    UserRaffleOrderEntity orderEntity = raffleActivityPartakeService.createOrder(userId, activityId);
    
    // 2. 抽奖策略 - 执行抽奖
    RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder()
        .userId(orderEntity.getUserId())
        .strategyId(orderEntity.getStrategyId())
        .endDateTime(orderEntity.getEndDateTime())
        .build());
    
    // 3. 存放结果 - 写入中奖记录
    UserAwardRecordEntity userAwardRecord = UserAwardRecordEntity.builder()
        .userId(orderEntity.getUserId())
        .awardId(raffleAwardEntity.getAwardId())
        .awardTitle(raffleAwardEntity.getAwardTitle())
        .build();
    awardService.saveUserAwardRecord(userAwardRecord);
    
    // 4. 返回结果
    return Response.builder().data(ActivityDrawResponseDTO.builder()
        .awardId(raffleAwardEntity.getAwardId())
        .awardTitle(raffleAwardEntity.getAwardTitle())
        .build()).build();
}
```

**注意事项**：
1. **订单幂等性**：通过查询未使用的订单记录，避免重复创建
2. **账户额度控制**：三层账户（总/月/日）依次扣减
3. **事务一致性**：聚合对象保证原子性
4. **分库分表**：根据用户ID路由到不同的数据库

### 4.2 抽奖策略执行流程

**流程图**：

```
performRaffle()
    │
    ├── 1. 参数校验
    │
    ├── 2. 责任链计算【raffleLogicChain】
    │       ├── 黑名单判断 → 命中则返回
    │       ├── 权重规则 → 命中则返回
    │       └── 默认抽奖 → 返回奖品ID
    │
    ├── 3. 决策树过滤【raffleLogicTree】
    │       ├── 次数锁节点 → 判断是否达到次数
    │       ├── 库存扣减节点 → 扣减库存
    │       └── 兜底奖品节点 → 返回最终奖品
    │
    └── 4. 返回抽奖结果
```

**核心代码**：`domain/strategy/service/raffle/DefaultRaffleStrategy.java`

```java
@Override
public DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {
    // 1. 查询策略
    StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);
    
    // 2. 获取责任链
    ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
    
    // 3. 执行责任链
    return logicChain.logic(userId, strategyId);
}

@Override
public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId, Date endDateTime) {
    // 1. 查询规则树配置
    RuleTreeVO ruleTreeVO = repository.queryRuleTreeVOByTreeId(strategyEntity.getRuleTreeId());
    
    // 2. 执行决策树
    IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);
    return treeEngine.process(userId, strategyId, awardId, endDateTime);
}
```

**注意事项**：
1. **规则配置化**：责任链和决策树的规则通过数据库配置
2. **规则缓存**：责任链和决策树实例会被缓存
3. **熔断兜底**：决策树最后有兜底奖品节点

### 4.3 积分兑换商品流程

**流程图**：

```
用户请求兑换 → 创建SKU订单 → 选择交易策略 → 积分支付 → 更新账户额度
```

**核心代码**：`trigger/http/RaffleActivityController.creditPayExchangeSku()`

```java
@RequestMapping(value = "credit_pay_exchange_sku", method = RequestMethod.POST)
public Response<Boolean> creditPayExchangeSku(SkuProductShopCartRequestDTO request) {
    // 1. 创建兑换商品SKU订单
    UnpaidActivityOrderEntity order = raffleActivityAccountQuotaService.createOrder(
        SkuRechargeEntity.builder()
            .userId(request.getUserId())
            .sku(request.getSku())
            .orderTradeType(OrderTradeTypeVO.credit_pay_trade)
            .build());
    
    // 2. 积分支付订单
    String orderId = creditAdjustService.createOrder(TradeEntity.builder()
        .userId(order.getUserId())
        .tradeName(TradeNameVO.CONVERT_SKU)
        .tradeType(TradeTypeVO.REVERSE)  // 逆向扣减
        .amount(order.getPayAmount().negate())
        .build());
    
    return Response.builder().data(true).build();
}
```

**注意事项**：
1. **交易策略选择**：根据订单类型选择不同的交易策略
2. **积分逆向扣减**：兑换商品时积分是逆向交易
3. **订单状态管理**：待支付 → 完成

### 4.4 行为返利流程

**流程图**：

```
用户行为触发 → 查询返利配置 → 创建返利订单+MQ任务 → 存储聚合 → 发送MQ → 消费处理
```

**核心代码**：`domain/rebate/service/BehaviorRebateService.java`

```java
public List<String> createOrder(BehaviorEntity behaviorEntity) {
    // 1. 查询返利配置
    List<DailyBehaviorRebateVO> configs = behaviorRebateRepository.queryDailyBehaviorRebateConfig(behaviorEntity.getBehaviorTypeVO());
    
    // 2. 构建聚合对象
    for (DailyBehaviorRebateVO config : configs) {
        BehaviorRebateOrderEntity order = BehaviorRebateOrderEntity.builder()
            .userId(behaviorEntity.getUserId())
            .rebateType(config.getRebateType())
            .rebateConfig(config.getRebateConfig())
            .build();
        
        // 构建MQ消息任务
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTopic(sendRebateMessageEvent.topic());
        taskEntity.setMessage(rebateMessage);
        
        BehaviorRebateAggregate aggregate = BehaviorRebateAggregate.builder()
            .behaviorRebateOrderEntity(order)
            .taskEntity(taskEntity)
            .build();
    }
    
    // 3. 存储聚合对象
    behaviorRebateRepository.saveUserRebateRecord(userId, behaviorRebateAggregates);
}
```

**注意事项**：
1. **聚合保证一致性**：返利订单与MQ任务在同一事务
2. **补偿机制**：通过任务表记录消息，定时任务补偿发送

### 4.5 MQ消息补偿机制

**定时任务扫描**：`trigger/job/SendMessageTaskJob.java`

```java
@XxlJob("SendMessageTaskJob_DB1")
public void exec_db01() {
    RLock lock = redissonClient.getLock("big-market-SendMessageTaskJob_DB1");
    try {
        // 查询未发送的任务
        List<TaskEntity> taskEntities = taskService.queryNoSendMessageTaskList();
        
        for (TaskEntity taskEntity : taskEntities) {
            try {
                taskService.sendMessage(taskEntity);  // 发送MQ
                taskService.updateTaskSendMessageCompleted(userId, messageId);  // 更新状态
            } catch (Exception e) {
                taskService.updateTaskSendMessageFail(userId, messageId);  // 标记失败
            }
        }
    } finally {
        lock.unlock();
    }
}
```

**补偿机制流程**：

```
创建任务 → 尝试发送MQ → 成功则标记完成 → 失败则标记失败 → 定时任务重试
```

**注意事项**：
1. **分布式锁**：防止多台服务器重复执行任务
2. **任务状态管理**：待发送、已发送、发送失败
3. **幂等性保证**：MQ消费端需要保证幂等性

---

## 五、DDD战术设计总结

### 5.1 战术设计元素应用

| 战术元素 | 应用位置 | 具体案例 |
|----------|----------|----------|
| **聚合** | 各领域model/aggregate | `CreatePartakeOrderAggregate`, `TradeAggregate`, `GiveOutPrizesAggregate` |
| **实体** | 各领域model/entity | `ActivityEntity`, `UserRaffleOrderEntity`, `StrategyEntity` |
| **值对象** | 各领域model/valobj | `ActivityStateVO`, `OrderStateVO`, `RuleLogicCheckTypeVO` |
| **领域服务** | 各领域service | `RaffleActivityPartakeService`, `CreditAdjustService` |
| **仓储** | domain定义接口，infrastructure实现 | `IActivityRepository`, `IStrategyRepository` |
| **领域事件** | 各领域adapter/event | `SendAwardMessageEvent`, `CreditAdjustSuccessMessageEvent` |

### 5.2 架构特点

#### 1. 依赖倒置原则

```
领域层（domain）定义接口
    ↓
基础设施层（infrastructure）实现接口
```

**好处**：
- 领域层不依赖具体技术实现
- 可以轻松切换不同的数据库、缓存等技术
- 便于单元测试

#### 2. 聚合事务

**原则**：一个聚合对象对应一个数据库事务

**示例**：
```java
// 聚合对象
CreatePartakeOrderAggregate {
    总账户、月账户、日账户、抽奖订单
}

// 事务保存
transactionTemplate.execute(status -> {
    更新总账户
    更新月账户
    更新日账户
    插入订单
});
```

#### 3. 事件驱动架构

**流程**：
```
领域事件 → MQ消息 → 消费者处理 → 最终一致性
```

**示例**：
- 发奖事件 → MQ → 发放奖品
- 积分调整事件 → MQ → 更新积分账户
- 返利事件 → MQ → 处理返利

#### 4. 补偿机制

**任务表设计**：
```sql
task表字段：
- task_id
- topic (MQ主题)
- message (消息内容)
- state (状态：待发送/已发送/失败)
- retry_count (重试次数)
```

**补偿流程**：
1. 业务操作时写入任务表
2. 发送MQ消息
3. 成功则标记完成
4. 失败则定时任务重试

#### 5. 分库分表

**路由策略**：
```java
@Resource
private IDBRouterStrategy dbRouter;

// 根据用户ID路由到不同的数据库
dbRouter.doRouter(userId);
```

**好处**：
- 用户维度分片，查询性能提升
- 数据分散存储，单表数据量可控

### 5.3 设计亮点

#### 1. 规则引擎 - 责任链 + 决策树组合

**责任链**：
- 前置规则过滤（黑名单、权重规则）
- 可配置的规则顺序
- 规则独立封装，易于扩展

**决策树**：
- 后置规则处理（次数锁、库存扣减）
- 树形结构支持复杂条件分支
- 节点统一接口，便于维护

**组合优势**：
- 责任链处理"或"逻辑（命中即返回）
- 决策树处理"且"逻辑（依次判断）
- 灵活配置，应对各种业务场景

#### 2. 策略模式 - 多维度扩展

**抽奖算法策略**：
- O(1)算法 vs O(logN)算法
- 根据概率范围自动选择

**交易策略**：
- 积分支付策略
- 返利无支付策略
- 不同交易类型不同处理

**奖品发放策略**：
- 积分奖品
- 优惠券奖品
- OpenAI账户调额

**设计优势**：
- 算法/策略可独立演化
- 新增策略只需实现接口
- 配置化选择策略

#### 3. 模板方法 - 标准流程定义

**抽奖流程模板**：
```
参数校验 → 责任链计算 → 决策树过滤 → 返回结果
```

**参与活动模板**：
```
活动查询 → 订单查询 → 账户过滤 → 订单构建 → 保存聚合
```

**设计优势**：
- 流程标准化，降低出错概率
- 子类只需实现差异化的逻辑
- 便于后续扩展新的业务场景

#### 4. 聚合根 - 保证业务一致性边界

**CreatePartakeOrderAggregate**：
- 绑定总账户、月账户、日账户、订单
- 一次事务保证四个对象的原子性更新

**TradeAggregate**：
- 绑定积分账户、订单、MQ任务
- 保证账户变动、订单创建、消息发送的一致性

**设计优势**：
- 聚合内强一致性
- 聚合间最终一致性
- 清晰的事务边界

#### 5. 防腐层 - 隔离外部依赖

**仓储接口**：
- 领域层定义接口
- 基础设施层实现接口
- 隔离数据库、缓存等技术细节

**设计优势**：
- 领域层纯净，不依赖具体技术
- 技术栈可替换
- 便于单元测试

---

## 六、注意事项与最佳实践

### 6.1 业务注意事项

#### 1. 幂等性设计

**场景**：用户重复点击抽奖按钮

**解决方案**：
```java
// 查询未使用的订单
UserRaffleOrderEntity order = queryNoUsedRaffleOrder(partakeRaffleActivityEntity);
if (null != order) return order;  // 直接返回已有订单
```

**关键点**：
- 数据库唯一索引（userId + activityId + orderId）
- 查询未使用订单避免重复创建

#### 2. 账户额度控制

**场景**：防止用户过度抽奖

**解决方案**：
- 三层账户：总账户、月账户、日账户
- 依次扣减，任何一层额度不足则失败
- 聚合事务保证三层账户原子性更新

**关键点**：
- 额度扣减在事务内完成
- Redis缓存账户信息，减少数据库查询
- 定时任务同步账户状态

#### 3. 分库分表路由

**场景**：用户数据分散存储

**解决方案**：
```java
dbRouter.doRouter(userId);  // 根据用户ID路由
transactionTemplate.execute(status -> {
    // 在路由后的数据库执行事务
});
```

**关键点**：
- 用户维度分片，保证用户数据在同一数据库
- 事务必须在路由后执行
- 跨库查询需要特殊处理

#### 4. MQ消息可靠性

**场景**：MQ消息发送失败

**解决方案**：
- 任务表记录消息
- 定时任务补偿发送
- 消费端幂等性保证

**关键点**：
- 消息发送与业务操作在同一事务
- 任务表记录消息状态
- 重试机制保证最终送达

### 6.2 技术注意事项

#### 1. 缓存策略

**读缓存**：
```java
// 先查缓存
ActivityEntity entity = redisService.getCacheValue(key);
if (null != entity) return entity;

// 查数据库
entity = dao.queryById(id);

// 写入缓存
redisService.setCacheValue(key, entity);
```

**写缓存**：
```java
// 先更新数据库
dao.update(entity);

// 删除缓存
redisService.remove(key);
```

**关键点**：
- 读多写少的场景适合缓存
- 更新时删除缓存，避免脏数据
- 缓存过期时间合理设置

#### 2. 分布式锁

**场景**：定时任务防止重复执行

**解决方案**：
```java
RLock lock = redissonClient.getLock("big-market-job");
try {
    if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
        // 执行任务
    }
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

**关键点**：
- 使用Redisson实现分布式锁
- 锁超时时间合理设置
- finally块释放锁

#### 3. 事务传播

**场景**：聚合对象保存

**解决方案**：
```java
transactionTemplate.execute(status -> {
    // 更新账户
    // 插入订单
    // 发送MQ
    return 1;
});
```

**关键点**：
- 聚合操作使用编程式事务
- 保证聚合内操作的原子性
- 避免大事务影响性能

### 6.3 设计模式使用注意事项

#### 1. 模板方法 vs 策略模式

**模板方法适用场景**：
- 流程骨架固定
- 部分步骤实现可变
- 需要控制子类扩展点

**策略模式适用场景**：
- 算法/策略可替换
- 算法之间独立，无共同流程
- 需要运行时切换策略

**错误示例**：
```java
// ❌ 使用策略模式实现固定流程
interface IProcess {
    void step1();
    void step2();
    void step3();
}

// 每个策略都要实现所有步骤，代码重复
class ProcessA implements IProcess {
    void step1() { ... }
    void step2() { ... }
    void step3() { ... }
}
```

**正确示例**：
```java
// ✅ 使用模板方法实现固定流程
abstract class AbstractProcess {
    void process() {
        step1();
        step2();
        step3();
    }
    abstract void step2();  // 子类只需实现变化的部分
}
```

#### 2. 责任链 vs 策略模式

**责任链适用场景**：
- 处理者之间有顺序依赖
- 需要依次判断，命中即返回
- 处理者可配置

**策略模式适用场景**：
- 策略之间独立，无顺序依赖
- 根据条件选择一个策略执行
- 策略可替换

**错误示例**：
```java
// ❌ 使用策略模式处理有顺序依赖的规则
if (strategyA.match()) {
    strategyA.execute();
} else if (strategyB.match()) {
    strategyB.execute();
}
// 顺序硬编码，不易维护
```

**正确示例**：
```java
// ✅ 使用责任链处理有顺序依赖的规则
chainA.appendNext(chainB).appendNext(chainC);
chainA.logic();  // 依次执行
```

#### 3. 工厂模式使用场景

**适用场景**：
- 创建逻辑复杂
- 需要缓存实例
- 客户端不需要知道创建细节

**过度使用示例**：
```java
// ❌ 简单对象使用工厂
class UserFactory {
    static User create() {
        return new User();  // 多此一举
    }
}
```

**正确使用示例**：
```java
// ✅ 复杂对象使用工厂
class ChainFactory {
    private Map<Long, ILogicChain> cache;
    
    ILogicChain create(Long strategyId) {
        if (cache.containsKey(strategyId)) {
            return cache.get(strategyId);
        }
        // 复杂的构建逻辑
        ILogicChain chain = buildChain(strategyId);
        cache.put(strategyId, chain);
        return chain;
    }
}
```

---

## 七、总结

### 7.1 核心设计思想

1. **DDD战术设计**：
   - 聚合保证一致性边界
   - 实体、值对象、领域服务各司其职
   - 仓储隔离技术细节

2. **设计模式组合**：
   - 模板方法定义流程骨架
   - 策略模式支持算法扩展
   - 责任链实现规则过滤
   - 组合模式构建决策树

3. **架构分层**：
   - 领域层专注业务逻辑
   - 基础设施层处理技术实现
   - 触发器层提供入口
   - 应用层组装和配置

### 7.2 关键技术点

1. **事务管理**：
   - 聚合对象对应一个事务
   - 编程式事务保证原子性
   - 分布式事务通过MQ+补偿实现最终一致性

2. **性能优化**：
   - Redis缓存热点数据
   - 分库分表分散压力
   - 算法策略优化（O(1) vs O(logN)）

3. **可靠性保障**：
   - 幂等性设计
   - 补偿机制
   - 分布式锁

### 7.3 学习建议

1. **理解DDD战术设计**：
   - 重点理解聚合的设计思想
   - 区分实体和值对象
   - 掌握仓储模式的作用

2. **掌握设计模式**：
   - 理解每个设计模式的适用场景
   - 分析为什么选择这个模式而不是其他模式
   - 学会组合使用多个设计模式

3. **业务与技术结合**：
   - 理解业务场景对技术选型的影响
   - 分析技术方案的权衡和取舍
   - 学习如何将设计模式应用到实际业务

---

## 附录：核心类路径索引

### 领域层（big-market-domain）

**活动领域**：
- 聚合：`domain/activity/model/aggregate/`
- 实体：`domain/activity/model/entity/`
- 领域服务：`domain/activity/service/`
- 仓储接口：`domain/activity/adapter/repository/`

**策略领域**：
- 抽奖策略：`domain/strategy/service/raffle/`
- 责任链：`domain/strategy/service/rule/chain/`
- 决策树：`domain/strategy/service/rule/tree/`
- 算法：`domain/strategy/service/armory/algorithm/`

**奖品领域**：
- 奖品发放：`domain/award/service/`
- 发放策略：`domain/award/service/distribute/`

**积分领域**：
- 积分调额：`domain/credit/service/adjust/`

**返利领域**：
- 行为返利：`domain/rebate/service/`

### 基础设施层（big-market-infrastructure）

- 仓储实现：`infrastructure/adapter/repository/`
- DAO：`infrastructure/dao/`
- Redis：`infrastructure/redis/`
- 网关：`infrastructure/gateway/`

### 触发器层（big-market-trigger）

- HTTP接口：`trigger/http/`
- MQ消费者：`trigger/listener/`
- 定时任务：`trigger/job/`
- RPC服务：`trigger/rpc/`
