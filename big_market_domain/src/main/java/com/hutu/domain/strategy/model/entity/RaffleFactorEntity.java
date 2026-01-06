package com.hutu.domain.strategy.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 抽奖因子实体：其实只需要策略id + 用户ID即可
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleFactorEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 用户ID */
    private String userId;
    /** 策略ID */
    private Long strategyId;
}
