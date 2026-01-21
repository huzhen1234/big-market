package com.hutu.types.enums;

import lombok.Getter;


/**
 * 策略奖品规则模型枚举
 */
@Getter
public enum StrategyRuleModelEnum {
    // 随机积分
    RANDOM_SCORE,
    // 抽奖次数解锁
    DRAW_TIMES_UNLOCK,
    // 幸运奖
    LUCK,
    // 黑名单奖品
    RULE_BLACKLIST
}
