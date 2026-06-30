-- 聊一聊功能：新增会话表和消息表
-- 在 MySQL 中执行此文件即可

CREATE TABLE IF NOT EXISTS `conversation` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `student_id`      BIGINT       NOT NULL COMMENT '学生用户ID',
    `hr_id`           BIGINT       NOT NULL COMMENT 'HR用户ID',
    `job_id`          BIGINT       DEFAULT NULL COMMENT '关联岗位ID',
    `job_title`       VARCHAR(100) DEFAULT NULL COMMENT '岗位名称快照',
    `enterprise_name` VARCHAR(100) DEFAULT NULL COMMENT '企业名称快照',
    `last_message`    VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息摘要',
    `last_message_at` DATETIME     DEFAULT NULL COMMENT '最后消息时间',
    `student_unread`  INT          DEFAULT 0 COMMENT '学生未读数',
    `hr_unread`       INT          DEFAULT 0 COMMENT 'HR未读数',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态: 1=活跃 0=已关闭',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_student` (`student_id`),
    INDEX `idx_hr` (`hr_id`),
    INDEX `idx_job` (`job_id`),
    UNIQUE KEY `uk_student_hr_job` (`student_id`, `hr_id`, `job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

CREATE TABLE IF NOT EXISTS `chat_message` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` BIGINT       NOT NULL COMMENT '会话ID',
    `sender_id`       BIGINT       NOT NULL COMMENT '发送者用户ID',
    `sender_name`     VARCHAR(50)  DEFAULT NULL COMMENT '发送者姓名快照',
    `sender_role`     VARCHAR(10)  NOT NULL COMMENT '发送者角色: STUDENT/HR',
    `receiver_id`     BIGINT       NOT NULL COMMENT '接收者用户ID',
    `content`         TEXT         NOT NULL COMMENT '消息内容',
    `msg_type`        VARCHAR(20)  DEFAULT 'TEXT' COMMENT '消息类型: TEXT/IMAGE/FILE',
    `file_url`        VARCHAR(500) DEFAULT NULL COMMENT '附件URL',
    `is_read`         TINYINT      DEFAULT 0 COMMENT '是否已读: 0=未读 1=已读',
    `read_at`         DATETIME     DEFAULT NULL COMMENT '阅读时间',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation` (`conversation_id`),
    INDEX `idx_created` (`conversation_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';
