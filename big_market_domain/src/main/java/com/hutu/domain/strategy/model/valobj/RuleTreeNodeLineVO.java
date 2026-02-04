package com.hutu.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则树节点连线
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleTreeNodeLineVO {

    /** 规则树ID */
    private Integer treeId;
    /** 规则Key节点 From  从哪里来 当前节点*/
    private String ruleNodeFrom;
    /** 规则Key节点 To  去哪里 下一个节点*/
    private String ruleNodeTo;
    /** 限定类型；1:=;2:>;3:<;4:>=;5<=;6:enum[枚举范围] */
    private RuleLimitTypeVO ruleLimitType;
    /** 限定值（到下个节点） */
    private RuleLogicCheckTypeVO ruleLimitValue;

}