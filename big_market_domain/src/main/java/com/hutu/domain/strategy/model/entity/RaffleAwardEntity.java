package com.hutu.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 抽奖奖品实体：也就是用户在抽奖之后最终返回的应该是策略商品的信息
 *
 * @author huzhen
 * @date 2025/12/24 21:42:35
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 策略ID */
    private Long strategyId;
    /** 奖品ID */
    private Long awardId;
    /** 奖品标题 */
    private String awardTitle;
    /** 奖品描述 */
    private String awardDesc;

}
