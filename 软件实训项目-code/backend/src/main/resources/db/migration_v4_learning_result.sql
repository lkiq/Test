-- ==========================================
-- V4: 学习成果测评模块 - 题库 + 结果表
-- ==========================================

CREATE TABLE IF NOT EXISTS learning_question (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    stage           VARCHAR(32) NOT NULL COMMENT '阶段：BASIC/FRAMEWORK/PROJECT/INTERVIEW/FINAL',
    skill_id        BIGINT COMMENT '关联技能ID',
    type            VARCHAR(16) DEFAULT 'SELECT' COMMENT '题型：SELECT/MULTI/CODE/ESSAY',
    difficulty      VARCHAR(8) DEFAULT 'MEDIUM' COMMENT 'EASY/MEDIUM/HARD',
    content         TEXT NOT NULL COMMENT '题目内容',
    options         JSON COMMENT '选项JSON（选择题必有）',
    answer          VARCHAR(512) NOT NULL COMMENT '答案',
    score           INT DEFAULT 5 COMMENT '分值',
    knowledge_point VARCHAR(128) COMMENT '考察知识点',
    explanation     TEXT COMMENT '答案解析',
    is_deleted      TINYINT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stage_skill (stage, skill_id),
    INDEX idx_stage_diff (stage, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习成果测评题库';

CREATE TABLE IF NOT EXISTS learning_result (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id          BIGINT NOT NULL COMMENT '用户ID',
    path_id          BIGINT COMMENT '学习路径ID',
    skill_id         BIGINT COMMENT '技能ID',
    type             VARCHAR(32) DEFAULT 'STAGE' COMMENT 'STAGE/FINAL',
    stage            VARCHAR(32) COMMENT 'BASIC/FRAMEWORK/PROJECT/INTERVIEW/FINAL',
    knowledge_score  DOUBLE COMMENT '理论得分',
    practice_score   DOUBLE COMMENT '实操得分',
    total_score      DOUBLE COMMENT '总分',
    level            VARCHAR(16) COMMENT '优秀/良好/一般/待提升',
    passed           TINYINT DEFAULT 0 COMMENT '1=通过',
    correct_count    INT DEFAULT 0 COMMENT '正确题数',
    total_count      INT DEFAULT 0 COMMENT '总题数',
    strengths        TEXT COMMENT '优势总结',
    weaknesses       TEXT COMMENT '薄弱环节',
    ai_analysis      JSON COMMENT 'AI分析',
    ai_suggestions   JSON COMMENT 'AI建议',
    start_time       DATETIME COMMENT '开始时间',
    submit_time      DATETIME COMMENT '提交时间',
    duration_minutes INT COMMENT '耗时',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_path_id (path_id),
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习成果测评结果表';
