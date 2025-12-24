package com.hutu.infrastructure.persistent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 策略配置表
 * @TableName strategy_config
 */
@TableName(value ="strategy_config")
@Data
public class StrategyConfig {
    /**
     * 主键，自增，非业务用
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 策略ID，业务键
     */
    private Long strategyId;

    /**
     * 策略描述
     */
    private String strategyDesc;

    /**
     * 创建人（用户ID、账号或系统标识）
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