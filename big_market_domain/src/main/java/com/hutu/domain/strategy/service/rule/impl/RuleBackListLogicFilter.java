package com.hutu.domain.strategy.service.rule.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.hutu.domain.strategy.model.entity.RuleActionEntity;
import com.hutu.domain.strategy.model.entity.RuleMatterEntity;
import com.hutu.domain.strategy.model.entity.StrategyGuaranteeEntity;
import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.service.cache.StrategyCacheService;
import com.hutu.domain.strategy.service.rule.ILogicFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 黑名单过滤
 */
@Slf4j
@Component
public class RuleBackListLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource
    private StrategyCacheService cacheService;

    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        // 获取黑名单策略
        Long strategyId = ruleMatterEntity.getStrategyId();
        StrategyGuaranteeEntity strategyGuaranteeEntity = cacheService.queryStrategyGuaranteeBlack(ruleMatterEntity.getStrategyId());
        String userId = ruleMatterEntity.getUserId();
        List<Long> backListUserIds = strategyGuaranteeEntity.getBackListUserIds();
        // 判断用户ID是否在黑名单中
        if (CollectionUtil.isNotEmpty(backListUserIds) && backListUserIds.contains(Long.parseLong(userId))) {
            log.info("策略ID，{}, 用户ID：{} 在黑名单中", strategyId,userId);
            // 获取黑名单策略的商品ID
            Long awardId = strategyGuaranteeEntity.getGuaranteeAwards().get(0).getAwardId();
            return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                    .data(RuleActionEntity.RaffleBeforeEntity.builder()
                            .strategyId(ruleMatterEntity.getStrategyId())
                            .awardId(awardId)
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