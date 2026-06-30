package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 模拟面试记录表 - 存储 AI 模拟面试的问答与评估报告
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("interview_record")
public class InterviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标岗位ID */
    private Long targetJobId;

    /** 面试类型：TECHNICAL/HR/COMPREHENSIVE */
    private String interviewType;

    /** 题目与回答详情（JSON） */
    private String questionJson;

    /** 评估报告（JSON） */
    private String reportJson;

    /** 综合评分 */
    private Double score;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
