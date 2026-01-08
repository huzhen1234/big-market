package com.hutu.domain.strategy.service.rule;

import com.hutu.domain.strategy.model.entity.RuleActionEntity;
import com.hutu.domain.strategy.model.entity.RuleMatterEntity;

/**
 * 抽奖逻辑过滤器
 * @param <T>
 */
public interface ILogicFilter<T extends RuleActionEntity.RaffleEntity> {

    RuleActionEntity<T> filter(RuleMatterEntity ruleMatterEntity);

}