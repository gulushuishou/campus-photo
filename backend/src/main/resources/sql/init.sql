-- =====================================================
-- 校园约拍平台 数据库初始化脚本
-- 数据库：campus_photo
-- =====================================================

CREATE DATABASE IF NOT EXISTS campus_photo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_photo;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`      VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname`      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `campus`        VARCHAR(50)  DEFAULT NULL COMMENT '校区',
    `role`          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/PHOTOGRAPHER/ADMIN',
    `credit_score`  INT          NOT NULL DEFAULT 100 COMMENT '信用积分',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0正常 1封禁',
    `create_time`   DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 摄影师扩展表
-- ----------------------------
DROP TABLE IF EXISTS `photographer`;
CREATE TABLE `photographer` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT       NOT NULL COMMENT '关联用户id',
    `audit_status` VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
    `intro`        VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `style_tags`   VARCHAR(200) DEFAULT NULL COMMENT '风格标签，逗号分隔',
    `works_count`  INT          NOT NULL DEFAULT 0 COMMENT '作品数量',
    `create_time`  DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摄影师扩展表';

-- ----------------------------
-- 3. 作品表
-- ----------------------------
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `photographer_id` BIGINT      NOT NULL COMMENT '摄影师id',
    `title`          VARCHAR(100) NOT NULL COMMENT '作品标题',
    `cover`          VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `images`         TEXT         DEFAULT NULL COMMENT '多图（JSON数组）',
    `style_tags`     VARCHAR(200) DEFAULT NULL COMMENT '风格标签，逗号分隔',
    `description`    VARCHAR(1000) DEFAULT NULL COMMENT '作品描述',
    `status`         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PUBLISHED/REMOVED',
    `view_count`     INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    `like_count`     INT          NOT NULL DEFAULT 0 COMMENT '点赞量',
    `create_time`    DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_time`    DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_photographer_id` (`photographer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品表';

-- ----------------------------
-- 4. 约拍需求表
-- ----------------------------
DROP TABLE IF EXISTS `demand`;
CREATE TABLE `demand` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT        NOT NULL COMMENT '发布用户id',
    `title`       VARCHAR(100)  NOT NULL COMMENT '需求标题',
    `content`     VARCHAR(1000) DEFAULT NULL COMMENT '需求内容',
    `style_tags`  VARCHAR(200)  DEFAULT NULL COMMENT '风格标签，逗号分隔',
    `shoot_time`  DATETIME      DEFAULT NULL COMMENT '拍摄时间',
    `location`    VARCHAR(200)  DEFAULT NULL COMMENT '拍摄地点',
    `budget`      DECIMAL(10,2) DEFAULT NULL COMMENT '预算',
    `status`      VARCHAR(20)   NOT NULL DEFAULT 'RECRUITING' COMMENT '状态：RECRUITING/DONE/CLOSED',
    `create_time` DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='约拍需求表';

-- ----------------------------
-- 5. 预约订单表
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`       VARCHAR(32)   NOT NULL COMMENT '订单号',
    `demand_id`      BIGINT        DEFAULT NULL COMMENT '需求id',
    `photographer_id` BIGINT       NOT NULL COMMENT '摄影师用户id',
    `user_id`        BIGINT        NOT NULL COMMENT '下单用户id',
    `status`         VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/CONFIRMED/FINISHED/EVALUATED/CANCELLED',
    `price`          DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
    `shoot_time`     DATETIME      DEFAULT NULL COMMENT '拍摄时间',
    `location`       VARCHAR(200)  DEFAULT NULL COMMENT '拍摄地点',
    `remark`         VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    `version`        INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`    DATETIME      DEFAULT NULL COMMENT '创建时间',
    `update_time`    DATETIME      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_photographer_id` (`photographer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

-- ----------------------------
-- 6. 评价表
-- ----------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`     BIGINT        NOT NULL COMMENT '订单id',
    `from_user_id` BIGINT        NOT NULL COMMENT '评价人用户id',
    `to_user_id`   BIGINT        NOT NULL COMMENT '被评价人用户id',
    `score`        INT           NOT NULL COMMENT '评分1-5',
    `content`      VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
    `credit_delta` INT           NOT NULL DEFAULT 0 COMMENT '信用分变动',
    `create_time`  DATETIME      DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ----------------------------
-- 7. 私信表
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `conversation_id` VARCHAR(100)  NOT NULL COMMENT '会话id（小id_大id）',
    `from_user_id`    BIGINT        NOT NULL COMMENT '发送人id',
    `to_user_id`      BIGINT        NOT NULL COMMENT '接收人id',
    `content`         VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `is_read`         TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    `create_time`     DATETIME      DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信表';

-- ----------------------------
-- 8. 收藏表
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '用户id',
    `work_id`     BIGINT   NOT NULL COMMENT '作品id',
    `create_time` DATETIME DEFAULT NULL COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_work` (`user_id`, `work_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ----------------------------
-- 初始测试数据
-- ----------------------------
-- 管理员账号：admin / admin123（BCrypt加密）
-- 摄影师账号：photographer / 123456
-- 普通用户账号：user / 123456
-- 说明：以下密码为占位，实际请用程序注册或后端生成
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `credit_score`, `status`, `create_time`, `update_time`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '管理员', 'ADMIN', 100, 0, NOW(), NOW()),
('photographer', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '校园摄影师小王', 'PHOTOGRAPHER', 100, 0, NOW(), NOW()),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '爱拍照的小李', 'USER', 100, 0, NOW(), NOW());

INSERT INTO `photographer` (`user_id`, `audit_status`, `intro`, `style_tags`, `works_count`, `create_time`, `update_time`) VALUES
(2, 'APPROVED', '擅长日系清新风与校园写真，三年拍摄经验', '日系,校园,清新', 3, NOW(), NOW());
