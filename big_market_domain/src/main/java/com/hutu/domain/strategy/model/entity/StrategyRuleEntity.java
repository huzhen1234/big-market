package com.hutu.domain.strategy.model.entity;
import com.hutu.types.enums.StrategyRuleModelEnum;
import lombok.Data;

/**
 * 策略奖品规则表

 */
@Data
public class StrategyRuleEntity {
    /**
     * 策略ID
     */
    private Long strategyId;

    /**
     * 奖品ID
     */
    private Long awardId;

    /**
     * 规则描述，如：抽奖3次后解锁该奖品
     */
    private String ruleDesc;

    /**
     * 奖品规则模型
     */
    private StrategyRuleModelEnum modelEnum;

    private Integer randomScore;          // 随机积分上限值
    private Integer drawTimesUnlock;      // 解锁所需抽奖次数
    private Integer luck;                 // 幸运奖数值
}