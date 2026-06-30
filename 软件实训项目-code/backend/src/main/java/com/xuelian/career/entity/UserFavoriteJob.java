package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户收藏岗位表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_favorite_job")
public class UserFavoriteJob {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 岗位ID */
    private Long jobId;

    /** 收藏时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
