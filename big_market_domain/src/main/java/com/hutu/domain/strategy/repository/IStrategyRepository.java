package com.hutu.domain.strategy.repository;

import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.model.entity.StrategyRuleEntity;
import com.hutu.domain.strategy.model.valobj.RuleTreeVO;
import com.hutu.domain.strategy.model.valobj.StrategyAwardStockKeyVO;

import java.util.List;

/**
 * 策略服务仓储接口
 *
 * @author huzhen
 * @date 2025/12/24 21:38
 */
public interface IStrategyRepository {

    /**
     * 查询策略奖品列表
     *
     * @param strategyId 策略id
     * @return 策略奖品列表
     */
    List<StrategyAwardEntity> queryAllStrategyAward(Long strategyId);



    /**
     * 根据策略id查询策略配置 权重
     * @param strategyId 策略id
     */
    List<StrategyGuaranteeEntity> queryStrategyGuaranteeWeight(Long strategyId);

    /**
     * 根据策略id查询策略配置 todo黑名单(黑名单属于较少用户，因此不加入缓存)
     * @param strategyId 策略id
     * @return 策略配置
     */
    StrategyGuaranteeEntity queryStrategyGuaranteeBlack(Long strategyId);


    /**
     * 根据策略ID获取策略类型 去重
     */
    List<String> queryStrategyType(Long strategyId);


    /**
     * todo 后续删除或者修改
     * 根据策略id和奖品id查询策略商品规则
     * 涵盖所有商品规则模型，但是对于抽奖中则只有 锁次数 + 幸运奖
     * @param strategyId 策略id
     * @param awardId 奖品id
     * @return 策略商品规则
     */
    StrategyRuleEntity findByStrategyIdAndAwardId(Long strategyId, Long awardId);

    /**
     * 查询策略商品规则模型
     * @param strategyId 策略id
     * @param awardId 奖品id
     * @return 策略商品规则模型
     */
    String queryStrategyAwardRuleModels(Long strategyId,Long awardId);


    /**
     * 根据规则树ID，查询树结构信息
     *
     * @param treeId 规则树ID
     * @return 树结构信息
     */
    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);



    /**
     * 减库存
     *
     * @param cacheKey 缓存key
     * @return 是否成功
     */
    Boolean subtractionAwardStock(String cacheKey);

    /**
     * 缓存奖品库存
     *
     * @param cacheKey   key
     * @param awardCount 库存值
     */
    void cacheStrategyAwardCount(String cacheKey, Integer awardCount);

    /**
     * 写入奖品库存消费队列
     *
     * @param strategyAwardStockKeyVO 对象值对象
     */
    void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO);

    /**
     * 获取奖品库存消费队列
     */
    StrategyAwardStockKeyVO takeQueueValue() throws InterruptedException;

    /**
     * 更新奖品库存消耗记录
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     */
    void updateStrategyAwardStock(Long strategyId, Long awardId);


}
