-- 经销商系统补充SQL
-- 用于补充经销商管理所需的数据库字段

USE `tourism_distribution_db`;

-- 1. 在 user 表中添加经销商钱包余额字段（用于缓存经销商的可用余额，提高查询效率）
ALTER TABLE `user`
    ADD COLUMN `wallet_balance` decimal(10, 2) DEFAULT '0.00' COMMENT '经销商钱包余额（仅分销商role=2有效）' AFTER `distributor_level`;

-- 2. 为 bus_commission_log 表添加更多字段以支持完整的账单流水
ALTER TABLE `bus_commission_log`
    ADD COLUMN `order_sn` varchar(64) DEFAULT NULL COMMENT '订单号（冗余字段，便于查询）' AFTER `order_id`,
    ADD COLUMN `scenic_name` varchar(100) DEFAULT NULL COMMENT '景区名称（冗余字段，便于展示）' AFTER `order_sn`,
    ADD COLUMN `balance_after` decimal(10, 2) DEFAULT NULL COMMENT '操作后余额' AFTER `profit_amount`,
    ADD COLUMN `log_type` tinyint DEFAULT '1' COMMENT '流水类型: 1-佣金收入, 2-提现支出, 3-平台费用' AFTER `log_remark`;

-- 3. 为便于统计，给 bus_order 表的 payment_time 添加索引
ALTER TABLE `bus_order`
    ADD INDEX `idx_payment_time` (`payment_time`),
    ADD INDEX `idx_distributor_status` (`distributor_id`, `order_status`);

-- 4. 创建景区分享统计表（可选，用于记录分享链接的点击和转化数据）
DROP TABLE IF EXISTS `bus_share_statistics`;
CREATE TABLE `bus_share_statistics`
(
    `id`             bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `distributor_id` bigint   NOT NULL COMMENT '分销商ID',
    `scenic_id`      bigint   NOT NULL COMMENT '景区ID',
    `share_code`     varchar(32) DEFAULT NULL COMMENT '分享码（可选，用于追踪）',
    `click_count`    int      DEFAULT '0' COMMENT '点击次数',
    `order_count`    int      DEFAULT '0' COMMENT '转化订单数',
    `share_date`     date     NOT NULL COMMENT '统计日期',
    `create_time`    datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_distributor_scenic_date` (`distributor_id`, `scenic_id`, `share_date`),
    KEY `idx_distributor_id` (`distributor_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='业务-分享统计表';

-- 说明：
-- 1. wallet_balance 字段用于缓存经销商的当前余额，每次佣金入账时更新
-- 2. bus_commission_log 表的补充字段用于完整记录账单流水信息
-- 3. bus_share_statistics 表用于统计分享效果（可选实现）
-- 4. 索引优化用于提升订单和佣金统计的查询性能
