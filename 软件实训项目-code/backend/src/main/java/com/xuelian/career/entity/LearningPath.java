package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 学习路径表 - 存储为用户生成的个性化学习计划
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("learning_path")
public class LearningPath {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标岗位ID */
    private Long targetJobId;

    /** 每日学习小时数 */
    private Double dailyHours;

    /** 预计总天数 */
    private Integer totalDays;

    /** 状态：ACTIVE/COMPLETED/ARCHIVED */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
