package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 测评题库表 - 存储五维能力测评题目
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("assessment_question")
public class AssessmentQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 测评维度：PROGRAMMING/LOGIC/PRODUCT/TECH/COMMUNICATION */
    private String dimension;

    /** 题目类型：选择题/判断题 */
    private String type;

    /** 题目内容 */
    private String content;

    /** 选项列表（JSON数组） */
    private String options;

    /** 正确答案 */
    private String answer;

    /** 题目分值 */
    private Integer score;

    /** 难度：简单/中等/困难 */
    private String difficulty;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
