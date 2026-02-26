package com.hutu.domain.strategy.service.rule.tree.impl;

import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.model.valobj.TreeActionEntity;
import com.hutu.domain.strategy.service.rule.tree.ILogicTreeNode;
import com.hutu.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 兜底奖励节点
 */
@Slf4j
@Component("rule_luck_award")
public class RuleLuckAwardLogicTreeNode implements ILogicTreeNode {

    @Override
    public TreeActionEntity logic(Long userId, Long strategyId, Long awardId,String ruleValue) {
        log.info("规则过滤-兜底奖品 userId:{} strategyId:{} awardId:{} ruleValue:{}", userId, strategyId, awardId, ruleValue);
        String[] split = ruleValue.split(Constants.COLON); // 01:1,100
        if (split.length == 0) {
            log.error("规则过滤-兜底奖品，兜底奖品未配置告警 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
            throw new RuntimeException("兜底奖品未配置 " + ruleValue);
        }
        // 兜底奖励配置
        Long luckAwardId = Long.valueOf(split[0]);
        String awardRuleValue = split.length > 1 ? split[1] : "";
        // 返回兜底奖品
        log.info("规则过滤-兜底奖品 userId:{} strategyId:{} awardId:{} awardRuleValue:{}", userId, strategyId, luckAwardId, awardRuleValue);
        return TreeActionEntity.builder()
                .ruleLogicCheckType(RuleLogicCheckTypeVO.TAKE_OVER)
                .strategyAwardVO(TreeActionEntity.StrategyAwardVO.builder()
                        .awardId(luckAwardId)
                        .awardRuleValue(awardRuleValue) // todo
                        .build())
                .build();
    }

}