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

}
