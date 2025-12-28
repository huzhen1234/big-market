package com.hutu.domain.strategy.repository;

import com.hutu.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

/**
 * 策略仓储接口
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
}
