-- ==========================================
-- 数据库迁移 V2：注册优化 + 企业表 + 邮箱唯一
-- ==========================================

USE career_platform;

-- 1. user 表新增个人字段
ALTER TABLE `user`
    ADD COLUMN `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名' AFTER `username`,
    ADD COLUMN `education` VARCHAR(20) DEFAULT NULL COMMENT '学历：BACHELOR/MASTER/PHD/JUNIOR' AFTER `email`,
    ADD COLUMN `school` VARCHAR(100) DEFAULT NULL COMMENT '毕业院校' AFTER `education`,
    ADD COLUMN `major` VARCHAR(100) DEFAULT NULL COMMENT '专业' AFTER `school`;

-- 2. 邮箱添加唯一约束（先清理可能的重复空值）
UPDATE `user` SET `email` = NULL WHERE `email` = '';
ALTER TABLE `user` ADD UNIQUE KEY `uk_email` (`email`);

-- 3. 新建企业信息表
CREATE TABLE IF NOT EXISTS `enterprise` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '企业ID',
    `hr_user_id` BIGINT NOT NULL COMMENT '关联的HR用户ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `company_industry` VARCHAR(50) DEFAULT NULL COMMENT '所属行业',
    `company_size` VARCHAR(20) DEFAULT NULL COMMENT '公司规模：1-50/51-200/201-500/501-1000/1000+',
    `company_address` VARCHAR(200) DEFAULT NULL COMMENT '公司地址',
    `company_description` TEXT DEFAULT NULL COMMENT '公司简介',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_position` VARCHAR(50) DEFAULT NULL COMMENT '联系人职位',
    `verify_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '认证状态：PENDING/APPROVED/REJECTED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hr_user_id` (`hr_user_id`),
    INDEX `idx_verify_status` (`verify_status`),
    INDEX `idx_company_name` (`company_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业信息表';

-- 4. user 表补充 is_deleted 列（与原实体 @TableLogic 对齐）
ALTER TABLE `user`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常/1删除）' AFTER `updated_at`;
