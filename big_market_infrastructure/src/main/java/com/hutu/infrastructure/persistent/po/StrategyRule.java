package com.hutu.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 策略奖品规则表
 * @TableName strategy_rule
 */
@TableName(value ="strategy_rule")
@Data
public class StrategyRule {
    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 策略ID
     */
    private Long strategyId;

    /**
     * 奖品ID
     */
    private Long awardId;

    /**
     * 规则模型编码，如：DRAW_TIMES_UNLOCK, LIKE_COUNT_UNLOCK
     */
    private String ruleModel;

    /**
     * 规则参数值，如：3（表示抽奖3次）
     */
    private String ruleParam;

    /**
     * 规则描述，如：抽奖3次后解锁该奖品
     */
    private String ruleDesc;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 最后更新人
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date updateTime;
}