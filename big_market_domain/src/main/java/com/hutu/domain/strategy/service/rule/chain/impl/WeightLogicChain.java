package com.hutu.domain.strategy.service.rule.chain.impl;

import com.hutu.domain.strategy.service.armory.IStrategyService;
import com.hutu.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.hutu.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.hutu.types.common.Constants.RULE_WEIGHT;

/**
 * 权重链
 */
@Slf4j
@Component(RULE_WEIGHT)
public class WeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyService strategyService;

    @Override
    public DefaultChainFactory.StrategyAwardVO doChain(Long strategyId, Long userId) {
        Long awardId = strategyService.findWeightStrategyAwardId(strategyId, userId);
        if (awardId != null && awardId > 0){
            log.info("权重抽奖，中奖奖品：{}", awardId);
            return DefaultChainFactory.StrategyAwardVO.builder()
                    .awardId(awardId)
                    .logicModel(RULE_WEIGHT)
                    .build();
        }
        // 继续执行下一个链
        log.info("权重抽奖，放行");
        return getLogic().doChain(strategyId, userId);
    }
}
