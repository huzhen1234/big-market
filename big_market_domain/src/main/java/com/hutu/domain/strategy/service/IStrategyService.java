package com.hutu.domain.strategy.service;

public interface IStrategyService {

    /**
     * 根据策略ID获取对应概率的奖品ID
     * @param strategyId
     * @return
     */
    Long findStrategyAwardId(Long strategyId);
}
