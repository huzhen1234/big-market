package com.hutu.domain.strategy.model.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 策略保底配置表
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
    private Object guaranteeAwards;
}