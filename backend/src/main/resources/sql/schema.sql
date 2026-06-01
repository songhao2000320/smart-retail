-- =====================================================
-- 智能零售用户行为分析系统 - 数据库初始化脚本
-- =====================================================

CREATE DATABASE IF NOT EXISTS retail_behavior
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE retail_behavior;

-- =====================================================
-- 1. 用户表 (user)
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT 'MD5 加密密码',
    `role` VARCHAR(20) NOT NULL DEFAULT 'analyst' COMMENT '角色：admin/manager/analyst',
    `store_id` BIGINT NULL COMMENT '所属门店ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=正常, 0=锁定',
    `locked_until` DATETIME NULL COMMENT '锁定截止时间',
    `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_username` (`username`),
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 2. 门店表 (store)
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_store` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '门店名称',
    `address` VARCHAR(255) NULL COMMENT '门店地址',
    `phone` VARCHAR(20) NULL COMMENT '联系电话',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=正常, 0=已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店表';

-- =====================================================
-- 3. 功能区域表 (zone)
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_zone` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `store_id` BIGINT NOT NULL COMMENT '所属门店ID',
    `name` VARCHAR(50) NOT NULL COMMENT '区域名称',
    `pos_x` DOUBLE NOT NULL COMMENT '平面 X 坐标（百分比 0-100）',
    `pos_y` DOUBLE NOT NULL COMMENT '平面 Y 坐标（百分比 0-100）',
    `width` DOUBLE NOT NULL COMMENT '区域宽度（百分比）',
    `height` DOUBLE NOT NULL COMMENT '区域高度（百分比）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=正常, 0=已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_store_id` (`store_id`),
    CONSTRAINT `fk_zone_store` FOREIGN KEY (`store_id`) REFERENCES `t_store`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能区域表';

-- =====================================================
-- 4. 顾客行为记录表 (customer_behavior)
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_customer_behavior` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `store_id` BIGINT NOT NULL COMMENT '所属门店ID',
    `entry_time` DATETIME NOT NULL COMMENT '进店时间',
    `leave_time` DATETIME NOT NULL COMMENT '离开时间',
    `is_purchased` TINYINT NOT NULL DEFAULT 0 COMMENT '是否购买：1=是, 0=否',
    `created_by` BIGINT NOT NULL COMMENT '录入人ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=有效, 0=无效',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_store_entry` (`store_id`, `entry_time`),
    INDEX `idx_entry_time` (`entry_time`),
    INDEX `idx_store_status` (`store_id`, `status`),
    CONSTRAINT `fk_behavior_store` FOREIGN KEY (`store_id`) REFERENCES `t_store`(`id`),
    CONSTRAINT `fk_behavior_user` FOREIGN KEY (`created_by`) REFERENCES `t_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客行为记录表';

-- =====================================================
-- 5. 区域停留记录表 (zone_stay)
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_zone_stay` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `behavior_id` BIGINT NOT NULL COMMENT '关联行为记录ID',
    `zone_id` BIGINT NOT NULL COMMENT '停留区域ID',
    `entry_time` DATETIME NOT NULL COMMENT '进入该区域时间',
    `leave_time` DATETIME NOT NULL COMMENT '离开该区域时间',
    PRIMARY KEY (`id`),
    INDEX `idx_behavior_id` (`behavior_id`),
    INDEX `idx_zone_id` (`zone_id`),
    CONSTRAINT `fk_stay_behavior` FOREIGN KEY (`behavior_id`) REFERENCES `t_customer_behavior`(`id`),
    CONSTRAINT `fk_stay_zone` FOREIGN KEY (`zone_id`) REFERENCES `t_zone`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域停留记录表';

-- =====================================================
-- 6. 用户门店授权表 (user_store_auth)
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_user_store_auth` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `store_id` BIGINT NOT NULL COMMENT '授权门店ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_store_id` (`store_id`),
    UNIQUE INDEX `uk_user_store` (`user_id`, `store_id`),
    CONSTRAINT `fk_auth_user` FOREIGN KEY (`user_id`) REFERENCES `t_user`(`id`),
    CONSTRAINT `fk_auth_store` FOREIGN KEY (`store_id`) REFERENCES `t_store`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户门店授权表';
