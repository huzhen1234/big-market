package com.hutu.domain.strategy.service.rule.chain.impl;

import com.hutu.domain.strategy.service.armory.StrategyArmoryDispatch;
import com.hutu.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.hutu.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.hutu.types.common.Constants.RULE_DEFAULT;


/**
 * 默认链--原始概率
 */
@Slf4j
@Component(value = RULE_DEFAULT)
public class DefaultLogicChain extends AbstractLogicChain {

    @Resource
    private StrategyArmoryDispatch armoryDispatch;

    /**
     * 默认链--原始概率，抽的是原始的(未包含权重的)
     */
    @Override
    public DefaultChainFactory.StrategyAwardVO doChain(Long strategyId, Long userId) {
        log.info("兜底抽奖，放行");
        Long awardId = armoryDispatch.getRandomAwardId(strategyId);
        return DefaultChainFactory.StrategyAwardVO.builder()
                .awardId(awardId)
                .logicModel(RULE_DEFAULT)
                .build();
    }
}
