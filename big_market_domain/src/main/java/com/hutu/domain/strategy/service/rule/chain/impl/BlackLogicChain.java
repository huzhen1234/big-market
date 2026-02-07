package com.hutu.domain.strategy.service.rule.chain.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.repository.cache.StrategyCacheService;
import com.hutu.domain.strategy.service.rule.chain.AbstractLogicChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.hutu.types.common.Constants.RULE_BLACKLIST;

/**
 * 黑名单链
 */
@Slf4j
@Component(value = RULE_BLACKLIST)
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
        log.info("策略ID，{}, 用户ID：{} 不在黑名单中", strategyId, userId);
        // 继续执行下一个链
        return getLogic().doChain(strategyId, userId);
    }
}
