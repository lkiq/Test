package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 岗位投递记录表 - 记录学生对岗位的投递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("job_application")
public class JobApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位ID */
    private Long jobId;

    /** 投递用户ID */
    private Long userId;

    /** 使用的简历ID */
    private Long resumeId;

    /** 状态：PENDING/INTERVIEW/REJECT/CANCELLED */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
