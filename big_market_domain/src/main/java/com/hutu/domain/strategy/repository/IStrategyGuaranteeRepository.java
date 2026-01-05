package com.hutu.domain.strategy.repository;

import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;

import java.util.List;

/**
 * 策略权重(兜底配置)
 */
public interface IStrategyGuaranteeRepository {

    /**
     * 根据策略id查询策略配置
     * @param strategyId 策略id
     */
    List<StrategyGuaranteeEntity> queryStrategyGuarantee(Long strategyId);
}
