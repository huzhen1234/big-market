package com.hutu.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 策略奖品详情表
 * @TableName strategy_award
 */
@TableName(value ="strategy_award")
@Data
public class StrategyAward {
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
     * 奖品标题
     */
    private String awardTitle;

    /**
     * 奖品二级标题
     */
    private String awardSubtitle;

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
     * 排序值，越小越靠前
     */
    private Integer sort;

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