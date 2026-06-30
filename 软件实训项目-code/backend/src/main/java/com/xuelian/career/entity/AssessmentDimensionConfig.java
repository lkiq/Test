package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 能力测评维度分段配置表
 * 用于根据用户各维度得分动态匹配个性化分析文案与提升计划
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("assessment_dimension_config")
public class AssessmentDimensionConfig {

    /** 配置ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 维度代码：PROGRAMMING/LOGIC/PRODUCT/TECH/COMMUNICATION */
    private String dimensionCode;

    /** 维度中文名 */
    private String dimensionName;

    /** 分数段类型：ZERO/LOW/MID/HIGH/ELITE */
    private String levelType;

    /** 分数下限（含） */
    private Integer scoreMin;

    /** 分数上限（含） */
    private Integer scoreMax;

    /** 展示等级：待提升/一般/良好/优秀 */
    private String levelLabel;

    /** 该分数段的深度分析文案 */
    private String analysis;

    /** 优势维度：如何利用该优势弥补短板 */
    private String compensateStrategy;

    /** 优势/中等维度：落地应用方法（JSON数组） */
    private String applicationTemplates;

    /** 薄弱维度：入门夯实任务列表（JSON数组） */
    private String entryTasks;

    /** 薄弱维度：进阶提升任务列表（JSON数组） */
    private String advanceTasks;

    /** 薄弱维度：实战应用任务列表（JSON数组） */
    private String practiceTasks;

    /** 入门周期 */
    private String entryDuration;

    /** 进阶周期 */
    private String advanceDuration;

    /** 实战周期 */
    private String practiceDuration;

    /** 语气风格：ENCOURAGING/NEUTRAL */
    private String tone;

    /** 排序 */
    private Integer sortOrder;

    /** 逻辑删除 */
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
