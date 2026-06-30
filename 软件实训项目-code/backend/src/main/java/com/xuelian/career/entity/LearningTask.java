package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习任务表 - 存储学习路径下的具体任务项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("learning_task")
public class LearningTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学习路径ID */
    private Long pathId;

    /** 用户ID */
    private Long userId;

    /** 关联技能ID */
    private Long skillId;

    /** 任务标题 */
    private String title;

    /** 任务描述 */
    private String description;

    /** 学习资源URL */
    private String resourceUrl;

    /** 学习阶段：BASIC/FRAMEWORK/PROJECT/INTERVIEW */
    private String stage;

    /** 任务状态：NOT_STARTED/IN_PROGRESS/LEARNING_COMPLETED/TEST_PASSED(P1预留) */
    private String status;

    /** 排序编号 */
    private Integer sortOrder;

    /** 截止日期 */
    private LocalDate dueDate;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
