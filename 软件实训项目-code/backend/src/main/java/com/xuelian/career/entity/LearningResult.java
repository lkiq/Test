package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 学习成果测评结果表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("learning_result")
public class LearningResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 学习路径ID */
    private Long pathId;

    /** 技能ID */
    private Long skillId;

    /** 测评类型：STAGE/FINAL */
    private String type;

    /** 阶段 */
    private String stage;

    /** 理论得分 */
    private Double knowledgeScore;

    /** 实操得分 */
    private Double practiceScore;

    /** 总分 */
    private Double totalScore;

    /** 等级：优秀/良好/一般/待提升 */
    private String level;

    /** 是否通过 */
    private Integer passed;

    /** 正确题数 */
    private Integer correctCount;

    /** 总题数 */
    private Integer totalCount;

    /** 优势总结 */
    private String strengths;

    /** 薄弱环节 */
    private String weaknesses;

    /** AI分析（JSON） */
    private String aiAnalysis;

    /** AI建议（JSON） */
    private String aiSuggestions;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 耗时（分钟） */
    private Integer durationMinutes;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
