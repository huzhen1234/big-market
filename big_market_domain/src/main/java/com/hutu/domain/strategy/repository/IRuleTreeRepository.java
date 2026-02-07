package com.hutu.domain.strategy.repository;

import com.hutu.domain.strategy.model.valobj.RuleTreeVO;

/**
 * 根据库表构建规则树
 */
public interface IRuleTreeRepository {

    /**
     * 根据规则树ID，查询树结构信息
     *
     * @param treeId 规则树ID
     * @return 树结构信息
     */
    RuleTreeVO queryRuleTreeVOByTreeId(String treeId);
}
