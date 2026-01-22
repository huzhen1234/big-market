package com.hutu.domain.strategy.service.rule.chain.impl;

import com.hutu.domain.strategy.service.IStrategyService;
import com.hutu.domain.strategy.service.rule.chain.AbstractLogicChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 权重链
 */
@Slf4j
@Component
public class WeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyService strategyService;

    @Override
    public Long doChain(Long strategyId, Long userId) {
        return strategyService.findWeightStrategyAwardId(strategyId, userId);
    }
}
