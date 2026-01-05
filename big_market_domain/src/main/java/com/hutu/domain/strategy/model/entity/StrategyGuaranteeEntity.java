package com.hutu.domain.strategy.model.entity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 策略配置
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyGuaranteeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 策略ID
     */
    private Long strategyId;

    /**
     * 策略类型，如：RULE_WEIGHT
     */
    private String strategyType;

    /**
     * 触发条件，如：MIN_SCORE
     */
    private String triggerCondition;

    /**
     * 触发值，如：5000
     */
    private String triggerValue;

    /**
     * 保底奖品及权重，格式：[{"awardId":301,"weight":50},...]
     */
    private List<AwardWeight> guaranteeAwards;

    /**
     * 奖品权重内部类
     */
    @Data
    public static class AwardWeight implements Serializable{
        private static final long serialVersionUID = 1L;
        @JsonProperty("awardId")
        private Long awardId;

        @JsonProperty("weight")
        private Integer weight;
    }
}
