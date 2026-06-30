package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 学习资源库表 - 存储各技能、各阶段的学习资料
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("learning_resource")
public class LearningResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联技能ID */
    private Long skillId;

    /** 适用阶段：BASIC/FRAMEWORK/PROJECT/INTERVIEW */
    private String stage;

    /** 资源类型：ARTICLE/VIDEO/EXERCISE/PROJECT */
    private String type;

    /** 资源标题 */
    private String title;

    /** 资源链接 */
    private String url;

    /** 资源描述 */
    private String description;

    /** 难度等级 */
    private String difficulty;

    /** 预计学习小时数 */
    private Double estimatedHours;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
