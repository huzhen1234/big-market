package com.hutu.domain.strategy.service.rule.chain.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.service.cache.StrategyCacheService;
import com.hutu.domain.strategy.service.rule.chain.AbstractLogicChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 黑名单链
 */
@Slf4j
@Component
public class BlackLogicChain extends AbstractLogicChain {

    @Resource
    private StrategyCacheService cacheService;

    @Override
    public Long doChain(Long strategyId, Long userId) {
        StrategyGuaranteeEntity strategyGuaranteeEntity = cacheService.queryStrategyGuaranteeBlack(strategyId);
        List<Long> backListUserIds = strategyGuaranteeEntity.getBackListUserIds();
        // 判断用户ID是否在黑名单中
        if (CollectionUtil.isNotEmpty(backListUserIds) && backListUserIds.contains(userId)) {
            log.info("策略ID，{}, 用户ID：{} 在黑名单中", strategyId, userId);
            // 获取黑名单策略的商品ID
            return strategyGuaranteeEntity.getGuaranteeAwards().get(0).getAwardId();
        }
        return -1L;
    }
}
