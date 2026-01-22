package com.hutu.domain.strategy.service.rule.filter.impl;

import com.hutu.domain.strategy.model.entity.RuleActionEntity;
import com.hutu.domain.strategy.model.entity.RuleMatterEntity;
import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.service.IStrategyService;
import com.hutu.domain.strategy.service.annotation.LogicStrategy;
import com.hutu.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import com.hutu.domain.strategy.service.rule.filter.ILogicFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 抽奖前过滤--权重
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_WIGHT)
public class RuleWeightLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource
    private IStrategyService strategyService;


    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        // 获取权重策略商品
        Long strategyAwardId = strategyService.findWeightStrategyAwardId(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getUserId());
        if (strategyAwardId != null) {
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .data(RuleActionEntity.RaffleBeforeEntity.builder()
                            .strategyId(ruleMatterEntity.getStrategyId())
                            .awardId(strategyAwardId)
                            .build())
                    .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                    .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                    .build();
        }
        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();
    }
}