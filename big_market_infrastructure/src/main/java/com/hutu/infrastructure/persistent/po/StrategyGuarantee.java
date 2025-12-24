package com.hutu.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 策略保底配置表
 * @TableName strategy_guarantee
 */
@TableName(value ="strategy_guarantee")
@Data
public class StrategyGuarantee {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /**
     * 
     */
    private Integer enabled;

    /**
     * 
     */
    private String createBy;

    /**
     * 
     */
    private String updateBy;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;
}