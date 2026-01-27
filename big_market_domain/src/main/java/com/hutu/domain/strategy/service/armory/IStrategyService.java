package com.hutu.domain.strategy.service.armory;

public interface IStrategyService {

    /**
     * 根据策略ID获取对应概率的奖品ID 权重奖品
     * @param strategyId 策略ID
     * @return 奖品ID- 权重
     */
    Long findWeightStrategyAwardId(Long strategyId,Long userId);

    /**
     * 根据策略ID获取对应概率的奖品ID 原始抽奖策略
     * @param strategyId 策略ID
     * @return 奖品ID- 权重
     */
    Long findOriginStrategyAwardId(Long strategyId,Long userId);
}
