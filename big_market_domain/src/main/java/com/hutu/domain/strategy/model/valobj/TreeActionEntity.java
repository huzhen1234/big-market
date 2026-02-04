package com.hutu.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 决策树动作实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TreeActionEntity {

    // 规则引擎检查结果
    private RuleLogicCheckTypeVO ruleLogicCheckType;
    // 策略奖品数据
    private StrategyAwardData strategyAwardData;



    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardData {
        /** 抽奖奖品ID - 内部流转使用 */
        private Integer awardId;
        /** 抽奖奖品规则 */
        private String awardRuleValue;
    }
}
