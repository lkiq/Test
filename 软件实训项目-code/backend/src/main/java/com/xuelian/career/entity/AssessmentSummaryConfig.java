package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 能力测评综合评语模板表
 * 根据总分、优势维度数、薄弱维度数匹配个性化综合评语
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("assessment_summary_config")
public class AssessmentSummaryConfig {

    /** 配置ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 总分下限 */
    private Integer totalScoreMin;

    /** 总分上限 */
    private Integer totalScoreMax;

    /** 优势维度数下限 */
    private Integer strengthCountMin;

    /** 优势维度数上限 */
    private Integer strengthCountMax;

    /** 薄弱维度数下限 */
    private Integer weaknessCountMin;

    /** 薄弱维度数上限 */
    private Integer weaknessCountMax;

    /** 综合评语模板，支持占位符 */
    private String summaryTemplate;

    /** 语气风格：ENCOURAGING/NEUTRAL */
    private String tone;

    /** 逻辑删除 */
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
