package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 学习成果测评题库
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("learning_question")
public class LearningQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 阶段：BASIC/FRAMEWORK/PROJECT/INTERVIEW/FINAL */
    private String stage;

    /** 关联技能ID */
    private Long skillId;

    /** 题型：SELECT/MULTI/CODE/ESSAY */
    private String type;

    /** 难度：EASY/MEDIUM/HARD */
    private String difficulty;

    /** 题目内容 */
    private String content;

    /** 选项JSON（选择题必有） */
    private String options;

    /** 答案 */
    private String answer;

    /** 分值 */
    private Integer score;

    /** 考察知识点 */
    private String knowledgePoint;

    /** 答案解析 */
    private String explanation;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
