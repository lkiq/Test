package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 技能测试题库表 - 存储学习路径中各技能各阶段的测试题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("skill_test_question")
public class SkillTestQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 技能ID（关联skill表） */
    private Long skillId;

    /** 阶段：BASIC/FRAMEWORK/PROJECT/INTERVIEW */
    private String stage;

    /** 题目内容 */
    private String question;

    /** 选项列表（JSON数组） */
    private String options;

    /** 正确答案，如"A" */
    private String correctAnswer;

    /** 难度：简单/中等/困难 */
    private String difficulty;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
