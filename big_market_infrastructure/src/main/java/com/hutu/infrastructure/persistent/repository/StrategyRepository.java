package com.hutu.infrastructure.persistent.repository;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.model.entity.StrategyRuleEntity;
import com.hutu.domain.strategy.model.valobj.RuleLimitTypeVO;
import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.model.valobj.RuleTreeNodeLineVO;
import com.hutu.domain.strategy.model.valobj.RuleTreeNodeVO;
import com.hutu.domain.strategy.model.valobj.RuleTreeVO;
import com.hutu.domain.strategy.model.valobj.StrategyAwardStockKeyVO;
import com.hutu.domain.strategy.repository.IStrategyRepository;
import com.hutu.infrastructure.persistent.mapper.RuleTreeMapper;
import com.hutu.infrastructure.persistent.mapper.RuleTreeNodeLineMapper;
import com.hutu.infrastructure.persistent.mapper.RuleTreeNodeMapper;
import com.hutu.infrastructure.persistent.mapper.StrategyAwardMapper;
import com.hutu.infrastructure.persistent.mapper.StrategyGuaranteeMapper;
import com.hutu.infrastructure.persistent.mapper.StrategyRuleMapper;
import com.hutu.infrastructure.persistent.po.RuleTree;
import com.hutu.infrastructure.persistent.po.RuleTreeNode;
import com.hutu.infrastructure.persistent.po.RuleTreeNodeLine;
import com.hutu.infrastructure.persistent.po.StrategyAward;
import com.hutu.infrastructure.persistent.po.StrategyGuarantee;
import com.hutu.infrastructure.persistent.po.StrategyRule;
import com.hutu.infrastructure.persistent.redis.IRedisService;
import com.hutu.types.common.Constants;
import com.hutu.types.enums.StrategyRuleModelEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hutu.types.common.Constants.RULE_BLACKLIST;
import static com.hutu.types.common.Constants.RULE_MODEL_SPLIT;
import static com.hutu.types.common.Constants.RULE_WEIGHT;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StrategyRepository implements IStrategyRepository {

    private final StrategyAwardMapper awardMapper;
    private final IRedisService redisService;
    private final RuleTreeMapper ruleTreeMapper;
    private final RuleTreeNodeMapper ruleTreeNodeMapper;
    private final RuleTreeNodeLineMapper ruleTreeNodeLineMapper;
    private final StrategyGuaranteeMapper guaranteeMapper;
    private final StrategyRuleMapper strategyRuleMapper;
    private final StrategyAwardMapper strategyAwardMapper;


    /**
     * 根据策略id查询策略奖品集合 缓存
     *
     * @param strategyId 策略id
     */
    @Override
    public List<StrategyAwardEntity> queryAllStrategyAward(Long strategyId) {
        // Todo参数校验
        List<StrategyAwardEntity> strategyAwardEntities = new ArrayList<>();
        LambdaQueryWrapper<StrategyAward> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyAward::getStrategyId, strategyId);
        List<StrategyAward> strategyAwards = awardMapper.selectList(queryWrapper);
        for (StrategyAward strategyAward : strategyAwards) {
            strategyAwardEntities.add(StrategyAwardEntity.builder()
                    .strategyId(strategyAward.getStrategyId())
                    .awardId(strategyAward.getAwardId())
                    .awardCount(strategyAward.getAwardCount())
                    .awardRemainCount(strategyAward.getAwardRemainCount())
                    .winRate(strategyAward.getWinRate())
                    .build());
            // 为里面的奖品key映射奖品库存
            String key = String.format(
                    Constants.STRATEGY_AWARD_STOCK_KEY_TEMPLATE,
                    strategyAward.getStrategyId(),
                    strategyAward.getAwardId()
            );
            // 缓存奖品库存
            cacheStrategyAwardCount(key, strategyAward.getAwardRemainCount());
        }
        return strategyAwardEntities;
    }

    // 根据策略id查询策略配置 权重
    @Override
    public List<StrategyGuaranteeEntity> queryStrategyGuaranteeWeight(Long strategyId) {
        LambdaQueryWrapper<StrategyGuarantee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyGuarantee::getStrategyId, strategyId)
                .eq(StrategyGuarantee::getStrategyType, RULE_WEIGHT)
                .orderByAsc(StrategyGuarantee::getTriggerValue); // 按 triggerValue 字段升序排列
        List<StrategyGuarantee> strategyGuarantee = guaranteeMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(strategyGuarantee)) {
            return strategyGuarantee.stream().map(tee -> StrategyGuaranteeEntity.builder()
                            .strategyId(tee.getStrategyId())
                            .triggerCondition(tee.getTriggerCondition())
                            .triggerValue(tee.getTriggerValue())
                            .guaranteeAwards(JSONUtil.toList(JSONUtil.toJsonStr(tee.getGuaranteeAwards()), StrategyGuaranteeEntity.AwardWeight.class))
                            .build())
                    .collect(Collectors.toList());
        }
        return CollectionUtil.empty(StrategyGuaranteeEntity.class);
    }

    @Override
    public StrategyGuaranteeEntity queryStrategyGuaranteeBlack(Long strategyId) {
        LambdaQueryWrapper<StrategyGuarantee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyGuarantee::getStrategyId, strategyId)
                .eq(StrategyGuarantee::getStrategyType, RULE_BLACKLIST);
        StrategyGuarantee strategyGuarantee = guaranteeMapper.selectOne(queryWrapper);
        return StrategyGuaranteeEntity.builder()
                .strategyId(strategyGuarantee.getStrategyId())
                .triggerCondition(strategyGuarantee.getTriggerCondition())
                .triggerValue(strategyGuarantee.getTriggerValue()) // 触发值 黑名单的值 [1,2,3]
                .guaranteeAwards(JSONUtil.toList(JSONUtil.toJsonStr(strategyGuarantee.getGuaranteeAwards()), StrategyGuaranteeEntity.AwardWeight.class))
                .backListUserIds(JSONUtil.toList(JSONUtil.toJsonStr(strategyGuarantee.getTriggerValue()), Long.class))
                .build();

    }

    // 根据策略ID获取策略类型 去重
    @Override
    public List<String> queryStrategyType(Long strategyId) {
        LambdaQueryWrapper<StrategyGuarantee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyGuarantee::getStrategyId, strategyId);
        List<StrategyGuarantee> strategyGuarantees = guaranteeMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(strategyGuarantees)) {
            return strategyGuarantees.stream().map(StrategyGuarantee::getStrategyType).distinct().collect(Collectors.toList());
        }
        return CollectionUtil.empty(String.class);
    }


    // todo 逻辑改动点 后续可能删除掉，只有单个，没有多个不用#连接
    @Override
    public StrategyRuleEntity findByStrategyIdAndAwardId(Long strategyId, Long awardId) {
        LambdaQueryWrapper<StrategyRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyRule::getStrategyId, strategyId)
                .eq(StrategyRule::getAwardId, awardId);
        StrategyRule strategyRule = strategyRuleMapper.selectOne(queryWrapper);
        if (strategyRule != null) {
            String ruleModel = strategyRule.getRuleModel();
            String ruleParam = strategyRule.getRuleParam();
            if (StringUtils.isNotEmpty(ruleModel)) {
                // 获取规则模型
                String[] models = ruleModel.split(RULE_MODEL_SPLIT);
                String[] ruleParams = ruleParam.split(RULE_MODEL_SPLIT);
                StrategyRuleEntity strategyRuleEntity = new StrategyRuleEntity();
                strategyRuleEntity.setStrategyId(strategyRule.getStrategyId());
                strategyRuleEntity.setAwardId(strategyRule.getAwardId());
                strategyRuleEntity.setRuleDesc(strategyRule.getRuleDesc());
                // 解析模型和参数，构建策略规则实体
                for (int i = 0; i < models.length && i < ruleParams.length; i++) {
                    String model = models[i];
                    String param = ruleParams[i];
                    StrategyRuleModelEnum modelEnum =
                            StrategyRuleModelEnum.valueOf(model.toUpperCase());
                    int paramValue = Integer.parseInt(param);
                    strategyRuleEntity.setModelEnum(modelEnum);
                    switch (modelEnum) {
                        case RANDOM_SCORE:
                            strategyRuleEntity.setRandomScore(paramValue);
                            return strategyRuleEntity;
                        case DRAW_TIMES_UNLOCK:
                            strategyRuleEntity.setDrawTimesUnlock(paramValue);
                            return strategyRuleEntity;
                        case LUCK:
                            strategyRuleEntity.setLuck(paramValue);
                            return strategyRuleEntity;
                        default:
                            break;
                    }
                }
                return strategyRuleEntity;
            }
        }
        return null;
    }

    @Override
    public String queryStrategyAwardRuleModels(Long strategyId, Long awardId) {
        LambdaQueryWrapper<StrategyRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrategyRule::getStrategyId, strategyId)
                .eq(StrategyRule::getAwardId, awardId);
        StrategyRule strategyRule = strategyRuleMapper.selectOne(queryWrapper);
        return strategyRule.getRuleModel();
    }

    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {
        // 从数据库获取
        LambdaQueryWrapper<RuleTree> ruleTreeWrapper = new LambdaQueryWrapper<>();
        ruleTreeWrapper.eq(RuleTree::getTreeId, treeId);
        RuleTree ruleTree = ruleTreeMapper.selectOne(ruleTreeWrapper);

        LambdaQueryWrapper<RuleTreeNode> treeNodeWrapper = new LambdaQueryWrapper<>();
        treeNodeWrapper.eq(RuleTreeNode::getTreeId, treeId);
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeMapper.selectList(treeNodeWrapper);

        LambdaQueryWrapper<RuleTreeNodeLine> treeNodeLineWrapper = new LambdaQueryWrapper<>();
        treeNodeLineWrapper.eq(RuleTreeNodeLine::getTreeId, treeId);

        List<RuleTreeNodeLine> ruleTreeNodeLines = ruleTreeNodeLineMapper.selectList(treeNodeLineWrapper);
        // 1. tree node line 转换Map结构
        Map<String, List<RuleTreeNodeLineVO>> ruleTreeNodeLineMap = new HashMap<>();
        for (RuleTreeNodeLine ruleTreeNodeLine : ruleTreeNodeLines) {
            RuleTreeNodeLineVO ruleTreeNodeLineVO = RuleTreeNodeLineVO.builder()
                    .treeId(ruleTreeNodeLine.getTreeId())
                    .ruleNodeFrom(ruleTreeNodeLine.getRuleNodeFrom())
                    .ruleNodeTo(ruleTreeNodeLine.getRuleNodeTo())
                    .ruleLimitType(RuleLimitTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitType()))
                    .ruleLimitValue(RuleLogicCheckTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitValue()))
                    .build();

            List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList = ruleTreeNodeLineMap.computeIfAbsent(ruleTreeNodeLine.getRuleNodeFrom(), k -> new ArrayList<>());
            ruleTreeNodeLineVOList.add(ruleTreeNodeLineVO);
        }

        // 2. tree node 转换为Map结构
        Map<String, RuleTreeNodeVO> treeNodeMap = new HashMap<>();
        for (RuleTreeNode ruleTreeNode : ruleTreeNodes) {
            RuleTreeNodeVO ruleTreeNodeVO = RuleTreeNodeVO.builder()
                    .treeId(ruleTreeNode.getTreeId())
                    .ruleKey(ruleTreeNode.getRuleKey())
                    .ruleDesc(ruleTreeNode.getRuleDesc())
                    .ruleValue(ruleTreeNode.getRuleValue())
                    .treeNodeLineVOList(ruleTreeNodeLineMap.get(ruleTreeNode.getRuleKey()))
                    .build();
            treeNodeMap.put(ruleTreeNode.getRuleKey(), ruleTreeNodeVO);
        }

        // 3. 构建 Rule Tree
        return RuleTreeVO.builder()
                .treeId(ruleTree.getTreeId())
                .treeName(ruleTree.getTreeName())
                .treeDesc(ruleTree.getTreeDesc())
                .treeRootRuleNode(ruleTree.getTreeNodeRuleKey())
                .treeNodeMap(treeNodeMap)
                .build();
    }


    @Override
    public Boolean subtractionAwardStock(String cacheKey) {
        long surplus = redisService.decr(cacheKey);
        if (surplus < 0) {
            // 库存小于0，恢复为0个
            redisService.setValue(cacheKey, 0);
            return false;
        }
        // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
        // 2. todo 加锁为了兜底，如果后续有恢复库存，手动处理等，也不会超卖。因为所有的可用库存key，都被加锁了。
        String lockKey = cacheKey + Constants.RULE_MODEL_SPLIT + surplus;
        Boolean lock = redisService.setNx(lockKey);
        if (!lock) {
            log.info("策略奖品库存加锁失败 {}", lockKey);
        }
        return lock;
    }


    @Override
    public void cacheStrategyAwardCount(String cacheKey, Integer awardCount) {
        if (redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, awardCount);
    }

    @Override
    public void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String cacheKey = Constants.STRATEGY_AWARD_STOCK_CONSUME_QUEUE_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<StrategyAwardStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(strategyAwardStockKeyVO, 3, TimeUnit.SECONDS);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueueValue() {
        String cacheKey = Constants.STRATEGY_AWARD_STOCK_CONSUME_QUEUE_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
        return destinationQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Long awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        LambdaUpdateWrapper<StrategyAward> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .setSql("award_remain_count = award_remain_count - 1")  // 设置awardRemainCount字段减1
                .eq(StrategyAward::getStrategyId, strategyId)           // where strategy_id = #{strategyId}
                .eq(StrategyAward::getAwardId, awardId)                 // and award_id = #{awardId}
                .gt(StrategyAward::getAwardRemainCount, 0);             // and award_remain_count > 0

        // 执行更新操作
        int updateResult = strategyAwardMapper.update(null, updateWrapper);
        // 可选：添加日志记录
        if (updateResult > 0) {
            log.info("成功更新奖品库存 strategyId:{} awardId:{} 减少1个", strategyId, awardId);
        } else {
            log.warn("更新奖品库存失败，可能库存已为0 strategyId:{} awardId:{}", strategyId, awardId);
        }
    }
}
