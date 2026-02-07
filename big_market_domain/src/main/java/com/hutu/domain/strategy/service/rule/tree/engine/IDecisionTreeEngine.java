package com.hutu.domain.strategy.service.rule.tree.engine;

import com.hutu.domain.strategy.model.valobj.RuleTreeVO;
import com.hutu.domain.strategy.model.valobj.TreeActionEntity;

/**
 * 决策树引擎
 * 规则树组合接口
 */
public interface IDecisionTreeEngine {

    TreeActionEntity.StrategyAwardVO process(String userId, Long strategyId, Integer awardId);


    RuleTreeVO buildRuleTree();


}