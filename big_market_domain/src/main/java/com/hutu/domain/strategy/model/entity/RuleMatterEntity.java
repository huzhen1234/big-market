package com.hutu.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 规则引擎物料
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleMatterEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 用户ID */
    private Long userId;
    /** 策略ID */
    private Long strategyId;
    /** 奖品ID -- 在抽奖中赋予值 */
    private Long awardId;
}