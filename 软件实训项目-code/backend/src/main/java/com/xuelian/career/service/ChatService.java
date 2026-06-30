package com.xuelian.career.service;

import com.xuelian.career.dto.response.ConversationResponse;
import com.xuelian.career.dto.response.MessageResponse;
import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /** 创建或获取已有会话 */
    ConversationResponse createOrGetConversation(Long studentId, Long hrId, Long jobId, String jobTitle, String enterpriseName);

    /** 发送消息 */
    MessageResponse sendMessage(Long conversationId, Long senderId, String senderRole, String senderName, String content, String msgType);

    /** 获取当前用户的会话列表 */
    List<ConversationResponse> getConversations(Long userId, String role);

    /** 获取会话历史消息（分页） */
    List<MessageResponse> getMessages(Long conversationId, Long userId, int page, int size);

    /** 标记会话已读 */
    void markAsRead(Long conversationId, Long userId, String role);

    /** 获取未读消息总数 */
    int getUnreadCount(Long userId, String role);

    /** 关闭会话 */
    void closeConversation(Long conversationId, Long userId);
}
