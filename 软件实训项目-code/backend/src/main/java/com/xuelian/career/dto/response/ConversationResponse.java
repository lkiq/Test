package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 会话列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long hrId;
    private String hrName;
    private Long jobId;
    private String jobTitle;
    private String enterpriseName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private Integer status;
    private LocalDateTime createdAt;
}
