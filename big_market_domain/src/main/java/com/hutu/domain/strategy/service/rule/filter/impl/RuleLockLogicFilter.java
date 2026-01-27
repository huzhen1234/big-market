package com.hutu.domain.strategy.service.rule.filter.impl;

import com.hutu.domain.strategy.model.entity.RuleActionEntity;
import com.hutu.domain.strategy.model.entity.RuleMatterEntity;
import com.hutu.domain.strategy.model.entity.StrategyRuleEntity;
import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.repository.IStrategyRuleRepository;
import com.hutu.domain.strategy.service.annotation.LogicStrategy;
import com.hutu.domain.strategy.service.rule.filter.factory.DefaultLogicFactory;
import com.hutu.domain.strategy.service.rule.filter.ILogicFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户抽奖n次后，对应奖品可解锁抽奖
 * 抽奖中过滤
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_LOCK)
public class RuleLockLogicFilter implements ILogicFilter<RuleActionEntity.RaffleCenterEntity> {

    @Resource
    private IStrategyRuleRepository ruleRepository;

    // todo用户抽奖次数，后续完成这部分流程开发的时候，从数据库/Redis中读取
    private Long userRaffleCount = 0L;

    @Override
    public RuleActionEntity<RuleActionEntity.RaffleCenterEntity> filter(RuleMatterEntity ruleMatterEntity) {
        log.info("规则过滤-次数锁 userId:{} strategyId:{}", ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId());
        StrategyRuleEntity ruleEntity = ruleRepository.findByStrategyIdAndAwardId(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId());
        Integer drawTimesUnlock = ruleEntity.getDrawTimesUnlock();
        // 用户抽奖次数大于规则限定值，或者是抽奖次数没有则 规则放行
        if (drawTimesUnlock == null || userRaffleCount >= drawTimesUnlock) {
            return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                    .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                    .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                    .build();
        }
        // 用户抽奖次数小于规则限定值，规则拦截
        return RuleActionEntity.<RuleActionEntity.RaffleCenterEntity>builder()
                .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                .build();
    }
}