package com.hutu.types.enums;

import lombok.Getter;
/**
 * 策略类型枚举
 */
@Getter
public enum StrategyEnum {
    // 权重策略 策略类型
    RULE_WEIGHT,
    // 触发条件类型 最小值
    MIN_SCORE,
    // 触发条件类型 最大值
    MAX_SCORE,
}
