package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 消息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private Long receiverId;
    private String content;
    private String msgType;
    private String fileUrl;
    private Integer isRead;
    private LocalDateTime createdAt;
}
