package com.hutu.domain.strategy.service.rule.chain.impl;

import com.hutu.domain.strategy.repository.cache.StrategyCacheService;
import com.hutu.domain.strategy.service.armory.IStrategyService;
import com.hutu.domain.strategy.service.rule.chain.AbstractLogicChain;
import com.hutu.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import com.hutu.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.Map;

import static com.hutu.types.common.Constants.RULE_WEIGHT;

/**
 * 权重链
 */
@Slf4j
@Component(RULE_WEIGHT)
public class WeightLogicChain extends AbstractLogicChain {

    @Resource
    private IStrategyService strategyService;
    @Resource
    private StrategyCacheService cacheService;

    @Override
    public DefaultChainFactory.StrategyAwardVO doChain(Long strategyId, Long userId) {
        // 初始化抽奖概率缓存
        strategyService.assembleLotteryStrategy(strategyId);
        String cacheKey = String.format(Constants.STRATEGY_AWARD_RATE_KEY_TEMPLATE, strategyId);
        // 获取缓存的概率Map
        Map<Long, BigDecimal> awardRateMap = cacheService.getStrategyAwardRate(cacheKey);
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
