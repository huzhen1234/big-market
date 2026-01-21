package com.hutu.domain.strategy.repository;

import com.hutu.domain.strategy.model.entity.StrategyRuleEntity;

/**
 * 策略商品规则仓储接口
 *
 * @author huzhen
 * @date 2025/12/24 21:38
 */
public interface IStrategyRuleRepository {

    /**
     * 根据策略id和奖品id查询策略商品规则
     * 涵盖所有商品规则模型，但是对于抽奖中则只有 锁次数 + 幸运奖
     * @param strategyId 策略id
     * @param awardId 奖品id
     * @return 策略商品规则
     */
    StrategyRuleEntity findByStrategyIdAndAwardId(Long strategyId,Long awardId);


}
