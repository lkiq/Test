package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 聊天会话表 - 学生与HR的一对一会话
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("conversation")
public class Conversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生用户ID */
    private Long studentId;

    /** HR用户ID */
    private Long hrId;

    /** 关联岗位ID（可为空） */
    private Long jobId;

    /** 岗位名称快照 */
    private String jobTitle;

    /** 企业名称快照 */
    private String enterpriseName;

    /** 最后一条消息摘要 */
    private String lastMessage;

    /** 最后消息时间 */
    private LocalDateTime lastMessageAt;

    /** 学生未读数 */
    private Integer studentUnread;

    /** HR未读数 */
    private Integer hrUnread;

    /** 状态: 1=活跃 0=已关闭 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
