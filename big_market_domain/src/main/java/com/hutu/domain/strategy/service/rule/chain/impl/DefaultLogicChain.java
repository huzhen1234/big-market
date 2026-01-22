package com.hutu.domain.strategy.service.rule.chain.impl;

import com.hutu.domain.strategy.service.IStrategyService;
import com.hutu.domain.strategy.service.rule.chain.AbstractLogicChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 兜底链
 */
@Slf4j
@Component
public class DefaultLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyService strategyService;

    /**
     * 兜底抽奖，抽的是原始的(未包含权重的)
     */
    @Override
    public Long doChain(Long strategyId, Long userId) {
        return strategyService.findOriginStrategyAwardId(strategyId, userId);
    }
}
