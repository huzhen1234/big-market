package com.hutu.domain.strategy.service.rule.tree.impl;

import com.hutu.domain.strategy.model.valobj.RuleLogicCheckTypeVO;
import com.hutu.domain.strategy.model.valobj.TreeActionEntity;
import com.hutu.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 兜底奖励节点
 */
@Slf4j
@Component("rule_luck_award")
public class RuleLuckAwardLogicTreeNode implements ILogicTreeNode {

    @Override
    public TreeActionEntity logic(Long userId, Long strategyId, Long awardId) {
        return TreeActionEntity.builder()
                .ruleLogicCheckType(RuleLogicCheckTypeVO.TAKE_OVER)
                .strategyAwardVO(TreeActionEntity.StrategyAwardVO.builder()
                        .awardId(101L)
                        .awardRuleValue("1,100")
                        .build())
                .build();
    }

}