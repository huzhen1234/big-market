CREATE TABLE `raffle_activity` (
                                   `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                   `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
                                   `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
                                   `activity_desc` varchar(128) NOT NULL COMMENT '活动描述',
                                   `begin_date_time` datetime NOT NULL COMMENT '开始时间',
                                   `end_date_time` datetime NOT NULL COMMENT '结束时间',
                                   `state` varchar(8) NOT NULL COMMENT '活动状态',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uq_activity_id` (`activity_id`),
                                   KEY `idx_begin_date_time` (`begin_date_time`),
                                   KEY `idx_end_date_time` (`end_date_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动表';

-- 活动表中的“库存总量”和“库存剩余”是指该活动允许用户参与的总次数限制，是一种“参与配额”的控制机制，而不是商品库存。
-- 它用于防止活动被过度使用，属于运营层面的宏观控制。而具体的奖品库存由策略商品表管理。两者是不同维度的“库存”。

CREATE TABLE `raffle_activity_count` (
                                         `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                         `activity_count_id` bigint(12) NOT NULL COMMENT '活动次数编号',
                                         `total_count` int(8) NOT NULL COMMENT '总次数',
                                         `day_count` int(8) NOT NULL COMMENT '日次数',
                                         `month_count` int(8) NOT NULL COMMENT '月次数',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uq_activity_count_id` (`activity_count_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动次数配置表';

CREATE TABLE `raffle_activity_sku` (
                                       `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                       `sku` bigint(12) NOT NULL COMMENT '商品sku - 把每一个组合当做一个商品',
                                       `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
                                       `activity_count_id` bigint(12) NOT NULL COMMENT '活动个人参与次数ID',
                                       `stock_count` int(11) NOT NULL COMMENT '商品库存',
                                       `stock_count_surplus` int(11) NOT NULL COMMENT '剩余库存',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uq_sku` (`sku`),
                                       KEY `idx_activity_id_activity_count_id` (`activity_id`,`activity_count_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `raffle_activity_order` (
                                         `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                         `user_id` varchar(32) NOT NULL COMMENT '用户ID',
                                         `sku` bigint(12) NOT NULL COMMENT '商品sku',
                                         `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
                                         `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
                                         `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
                                         `order_id` varchar(12) NOT NULL COMMENT '订单ID',
                                         `order_time` datetime NOT NULL COMMENT '下单时间',
                                         `total_count` int(8) NOT NULL COMMENT '总次数',
                                         `day_count` int(8) NOT NULL COMMENT '日次数',
                                         `month_count` int(8) NOT NULL COMMENT '月次数',
                                         `state` varchar(8) NOT NULL DEFAULT 'complete' COMMENT '订单状态（complete）',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uq_order_id` (`order_id`),
                                         KEY `idx_user_id_activity_id` (`user_id`,`activity_id`,`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动单';

CREATE TABLE `raffle_activity_account` (
                                           `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                           `user_id` varchar(32) NOT NULL COMMENT '用户ID',
                                           `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
                                           `total_count` int(8) NOT NULL COMMENT '总次数',
                                           `total_count_surplus` int(8) NOT NULL COMMENT '总次数-剩余',
                                           `day_count` int(8) NOT NULL COMMENT '日次数',
                                           `day_count_surplus` int(8) NOT NULL COMMENT '日次数-剩余',
                                           `month_count` int(8) NOT NULL COMMENT '月次数',
                                           `month_count_surplus` int(8) NOT NULL COMMENT '月次数-剩余',
                                           `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                           PRIMARY KEY (`id`),
                                           UNIQUE KEY `uq_user_id_activity_id` (`user_id`,`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动账户表';