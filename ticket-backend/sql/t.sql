CREATE DATABASE IF NOT EXISTS `tourism_distribution_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `tourism_distribution_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 用户表 - 小调整（角色编号建议更清晰）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_account` varchar(100) NOT NULL COMMENT '登录邮箱(账号)',
    `password`     varchar(100) NOT NULL COMMENT '加密存储的密码',
    `nickname`     varchar(64)           DEFAULT '新用户' COMMENT '用户昵称',
    `role`         tinyint      NOT NULL DEFAULT '1' COMMENT '角色: 0-管理员, 1-游客, 2-分销商',
    `user_status`  tinyint               DEFAULT '1' COMMENT '账号状态: 0-禁用, 1-正常',
    `audit_status` tinyint               DEFAULT '0' COMMENT '分销商审核状态: 0-待审核, 1-通过, 2-拒绝（仅分销商有效）',
    `create_time`  datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time`  datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`   tinyint               DEFAULT '0' COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account` (`user_account`),
    INDEX `idx_role` (`role`) -- 方便按角色查询
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户基础表';
-- 2. 景区信息表 (bus_scenic_spot)

DROP TABLE IF EXISTS `bus_scenic_spot`;
CREATE TABLE `bus_scenic_spot`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `spot_name`   varchar(100) NOT NULL COMMENT '景区名称',
    `description` text COMMENT '详情介绍',
    `address`     varchar(255) DEFAULT NULL COMMENT '地址',
    `open_time`   varchar(100) DEFAULT NULL COMMENT '开放时间说明',
    `contact_num` varchar(20)  DEFAULT NULL COMMENT '联系电话',
    `spot_status` tinyint      DEFAULT '1' COMMENT '状态: 1-开放, 0-关闭',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`  tinyint      DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='业务-景区信息表';

-- 3. 门票产品表 (bus_ticket_product)

DROP TABLE IF EXISTS `bus_ticket_product`;
CREATE TABLE `bus_ticket_product`
(
    `id`                      bigint         NOT NULL AUTO_INCREMENT,
    `spot_id`                 bigint         NOT NULL COMMENT '所属景区ID',
    `product_name`            varchar(100)   NOT NULL COMMENT '产品名称',
    `ticket_type`             tinyint        NOT NULL COMMENT '默认票种类型: 1-原价票, 2-半价票, 3-免票',
    `price_retail`            decimal(10, 2) NOT NULL COMMENT '官网指导零售价（平台最高限价）',
    `price_settlement`        decimal(10, 2) NOT NULL COMMENT '平台结算价（分销商最低成本价，佣金计算基准）',
    `default_dist_sale_price` decimal(10, 2) DEFAULT NULL COMMENT '分销商默认销售价（NULL则使用price_retail）',
    `sale_status`             tinyint        DEFAULT '1' COMMENT '上架状态: 1-上架, 0-下架',
    `create_time`             datetime       DEFAULT CURRENT_TIMESTAMP,
    `update_time`             datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`              tinyint        DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_spot_id` (`spot_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='业务-门票产品表';

-- 4. 景区日历库存表 (bus_scenic_stock)

DROP TABLE IF EXISTS `bus_scenic_stock`;
CREATE TABLE `bus_scenic_stock`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `scenic_id`    bigint NOT NULL COMMENT '关联景区ID',
    `stock_date`   date   NOT NULL COMMENT '库存日期',
    `total_qty`    int    NOT NULL DEFAULT '0' COMMENT '总库存量',
    `sold_qty`     int    NOT NULL DEFAULT '0' COMMENT '已售数量',
    `lock_version` int             DEFAULT '0' COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scenic_date` (`scenic_id`, `stock_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='业务-景区日历库存表';

-- 5. 订单主表 (bus_order)

DROP TABLE IF EXISTS `bus_order`;
CREATE TABLE `bus_order`
(
    `id`                    bigint         NOT NULL AUTO_INCREMENT,
    `order_sn`              varchar(64)    NOT NULL COMMENT '系统订单号',
    `buyer_id`              bigint         NOT NULL COMMENT '下单用户ID（游客）',
    `distributor_id`        bigint         DEFAULT NULL COMMENT '关联分销商ID（推广人）',
    `product_id`            bigint         NOT NULL COMMENT '产品ID',
    `visit_date`            date           NOT NULL COMMENT '游玩日期',
    `buy_num`               int            NOT NULL COMMENT '购买数量',
    `unit_price`            decimal(10, 2) NOT NULL COMMENT '下单时的实际销售单价（游客支付的价格）',
    `settlement_unit_price` decimal(10, 2) NOT NULL COMMENT '下单时的平台结算单价（快照）',
    `commission_amount`     decimal(10, 2) DEFAULT '0.00' COMMENT '本订单分销商佣金总额',

    `pay_amount`            decimal(10, 2) NOT NULL COMMENT '实付总额',
    `order_status`          tinyint        DEFAULT '0' COMMENT '订单状态: 0-待支付, 1-已支付待核销, 2-已核销, 3-已取消, 4-已退款, 5-已过期',
    `verification_code`     varchar(64)    DEFAULT NULL COMMENT '核销码（建议格式: 景区简称-日期-随机码）',
    `payment_time`          datetime       DEFAULT NULL COMMENT '支付完成时间',
    `verify_time`           datetime       DEFAULT NULL COMMENT '实际核销时间',
    `create_time`           datetime       DEFAULT CURRENT_TIMESTAMP,
    `update_time`           datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`            tinyint        DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`),
    KEY `idx_buyer` (`buyer_id`),
    KEY `idx_distributor` (`distributor_id`),
    KEY `idx_verification` (`verification_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='业务-订单主表';


-- 6. 游客实名详情表 (bus_order_visitor)

DROP TABLE IF EXISTS `bus_order_visitor`;
CREATE TABLE `bus_order_visitor`
(
    `id`           bigint      NOT NULL AUTO_INCREMENT,
    `order_id`     bigint      NOT NULL,
    `visitor_name` varchar(32) NOT NULL,
    `id_card`      varchar(32) NOT NULL,
    `contact_tel`  varchar(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='业务-订单游客实名表';

-- 7. 佣金记录明细表 (bus_commission_log)

DROP TABLE IF EXISTS `bus_commission_log`;
CREATE TABLE `bus_commission_log`
(
    `id`             bigint         NOT NULL AUTO_INCREMENT,
    `distributor_id` bigint         NOT NULL,
    `order_id`       bigint         NOT NULL,
    `profit_amount`  decimal(10, 2) NOT NULL COMMENT '结算佣金',
    `log_remark`     varchar(255) DEFAULT '门票销售佣金',
    `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_distributor_id` (`distributor_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='业务-佣金记录流水表';

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `user`
    ADD COLUMN `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像地址（URL）' AFTER `nickname`;

ALTER TABLE bus_scenic_spot
ADD COLUMN image_urls TEXT
    DEFAULT NULL
    COMMENT '景区图片URL数组(JSON格式)';

ALTER TABLE `bus_scenic_spot`
    ADD COLUMN `level`    VARCHAR(20)  DEFAULT NULL COMMENT '景区等级，如：5A、4A、国家级等' AFTER `spot_name`,
    ADD COLUMN `province` VARCHAR(50)  DEFAULT NULL COMMENT '景区所属省份（如：浙江省、北京市）' AFTER `level`;

-- 8. 新增：用户常用游客信息表（支持预填 + 默认票种）
DROP TABLE IF EXISTS `user_visitor`;
CREATE TABLE `user_visitor`
(
    `id`                  bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`             bigint      NOT NULL COMMENT '所属用户ID',
    `visitor_name`        varchar(32) NOT NULL COMMENT '游客姓名',
    `id_card`             varchar(32) NOT NULL COMMENT '身份证号（建议加密存储）',
    `contact_tel`         varchar(20) DEFAULT NULL COMMENT '联系电话（可选）',
    `default_ticket_type` tinyint     DEFAULT '1' COMMENT '默认票种类型: 1-原价票, 2-半价票, 3-免票',
    `is_default`          tinyint     DEFAULT '0' COMMENT '是否默认出行人：1-是，0-否',
    `create_time`         datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`          tinyint     DEFAULT '0' COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户常用游客信息表';

-- 在 user 表添加分销商等级字段（仅对 role=2 分销商有效）
ALTER TABLE `user`
    ADD COLUMN `distributor_level` tinyint DEFAULT '3' COMMENT '分销商等级: 1-金（提成0.3倍官网价）, 2-银（0.25倍）, 3-铜（0.2倍）, NULL-不适用' AFTER `role`;