package com.hutu.domain.strategy.service.rule.tree;

import com.hutu.domain.strategy.model.valobj.TreeActionEntity;

public interface ILogicTreeNode {

    TreeActionEntity logic(Long userId, Long strategyId, Long awardId);

}