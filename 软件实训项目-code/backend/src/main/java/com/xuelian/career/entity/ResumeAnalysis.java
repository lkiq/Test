package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 简历分析表 - 存储用户上传简历的 AI 分析结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_analysis")
public class ResumeAnalysis {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标岗位ID */
    private Long targetJobId;

    /** 上传的简历文件路径 */
    private String fileUrl;

    /** AI 综合评分 */
    private Double score;

    /** 分析结果详情（JSON） */
    private String resultJson;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
