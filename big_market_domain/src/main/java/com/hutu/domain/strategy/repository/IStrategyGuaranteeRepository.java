package com.hutu.domain.strategy.repository;

import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;

import java.util.List;

/**
 * 策略
 */
public interface IStrategyGuaranteeRepository {

    /**
     * 根据策略id查询策略配置 权重
     * @param strategyId 策略id
     */
    List<StrategyGuaranteeEntity> queryStrategyGuarantee(Long strategyId);
}
