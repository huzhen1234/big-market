package com.hutu.domain.strategy.model.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 策略奖品实体
 *
 * @author huzhen
 * @date 2025/12/24 21:42:35
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyAwardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 策略ID
     */
    private Long strategyId;

    /**
     * 奖品ID
     */
    private Long awardId;

    /**
     * 奖品总数量
     */
    private Integer awardCount;

    /**
     * 奖品剩余数量
     */
    private Integer awardRemainCount;

    /**
     * 中奖几率（0.0000~1.0000）
     */
    private BigDecimal winRate;

    /**
     * ⭐ 累计概率上限（运行期计算，不落库）
     * 例如：0.55 / 0.75 / 0.85 ...
     */
    private BigDecimal cumulativeRate;
}