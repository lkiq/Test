-- ==========================================
-- 数据库迁移 V3：岗位发布与管理功能
-- ==========================================

USE career_platform;

-- 1. job_position 表新增字段
ALTER TABLE `job_position`
    ADD COLUMN `publish_status` TINYINT NOT NULL DEFAULT 1 COMMENT '发布状态：0草稿/1已发布/2已下架' AFTER `education_required`,
    ADD COLUMN `hr_user_id` BIGINT DEFAULT NULL COMMENT '发布者HR用户ID' AFTER `publish_status`,
    ADD COLUMN `logo_url` VARCHAR(500) DEFAULT NULL COMMENT '公司Logo图片URL' AFTER `hr_user_id`,
    ADD COLUMN `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `logo_url`;

-- 2. 添加索引优化查询
ALTER TABLE `job_position`
    ADD INDEX `idx_publish_status` (`publish_status`),
    ADD INDEX `idx_hr_user` (`hr_user_id`);
