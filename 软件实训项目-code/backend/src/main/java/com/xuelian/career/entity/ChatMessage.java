package com.xuelian.career.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 聊天消息表 - 会话中的单条消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** 发送者用户ID */
    private Long senderId;

    /** 发送者姓名快照 */
    private String senderName;

    /** 发送者角色: STUDENT/HR */
    private String senderRole;

    /** 接收者用户ID */
    private Long receiverId;

    /** 消息内容 */
    private String content;

    /** 消息类型: TEXT/IMAGE/FILE */
    private String msgType;

    /** 附件URL */
    private String fileUrl;

    /** 是否已读: 0=未读 1=已读 */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readAt;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
