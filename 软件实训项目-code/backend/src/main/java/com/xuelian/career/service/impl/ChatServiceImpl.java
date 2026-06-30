package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.dto.response.ConversationResponse;
import com.xuelian.career.dto.response.MessageResponse;
import com.xuelian.career.entity.ChatMessage;
import com.xuelian.career.entity.Conversation;
import com.xuelian.career.common.BusinessException;
import com.xuelian.career.entity.User;
import com.xuelian.career.mapper.ChatMessageMapper;
import com.xuelian.career.mapper.ConversationMapper;
import com.xuelian.career.mapper.UserMapper;
import com.xuelian.career.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天服务实现 - 基于HTTP请求-响应的站内信模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ConversationResponse createOrGetConversation(Long studentId, Long hrId, Long jobId, String jobTitle, String enterpriseName) {
        // 先查是否已有会话
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getStudentId, studentId)
               .eq(Conversation::getHrId, hrId)
               .eq(jobId != null, Conversation::getJobId, jobId)
               .eq(Conversation::getStatus, 1);
        Conversation conv = conversationMapper.selectOne(wrapper);

        if (conv == null) {
            conv = new Conversation();
            conv.setStudentId(studentId);
            conv.setHrId(hrId);
            conv.setJobId(jobId);
            conv.setJobTitle(jobTitle);
            conv.setEnterpriseName(enterpriseName);
            conv.setStatus(1);
            conv.setStudentUnread(0);
            conv.setHrUnread(0);
            conversationMapper.insert(conv);
            log.info("创建新会话: id={}, student={}, hr={}", conv.getId(), studentId, hrId);
        }

        return toConversationResponse(conv, 0);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long conversationId, Long senderId, String senderRole, String senderName, String content, String msgType) {
        // 校验会话存在且活跃
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null || conv.getStatus() != 1) {
            throw new BusinessException(400, "会话不存在或已关闭");
        }

        // 校验发送者属于该会话
        if (!senderId.equals(conv.getStudentId()) && !senderId.equals(conv.getHrId())) {
            throw new BusinessException(403, "无权在该会话中发送消息");
        }

        Long receiverId = senderId.equals(conv.getStudentId()) ? conv.getHrId() : conv.getStudentId();

        // 保存消息
        ChatMessage msg = new ChatMessage();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setSenderName(senderName);
        msg.setSenderRole(senderRole);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setMsgType(msgType != null ? msgType : "TEXT");
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);

        // 更新会话
        conv.setLastMessage(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        conv.setLastMessageAt(LocalDateTime.now());
        if ("STUDENT".equals(senderRole)) {
            conv.setHrUnread(conv.getHrUnread() != null ? conv.getHrUnread() + 1 : 1);
        } else {
            conv.setStudentUnread(conv.getStudentUnread() != null ? conv.getStudentUnread() + 1 : 1);
        }
        conversationMapper.updateById(conv);

        return toMessageResponse(msg);
    }

    @Override
    public List<ConversationResponse> getConversations(Long userId, String role) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        if ("STUDENT".equals(role)) {
            wrapper.eq(Conversation::getStudentId, userId);
        } else {
            wrapper.eq(Conversation::getHrId, userId);
        }
        wrapper.eq(Conversation::getStatus, 1)
               .orderByDesc(Conversation::getLastMessageAt);

        List<Conversation> convs = conversationMapper.selectList(wrapper);

        // 批量查用户名
        List<Long> userIds = new ArrayList<>();
        for (Conversation c : convs) {
            userIds.add("STUDENT".equals(role) ? c.getHrId() : c.getStudentId());
        }
        Map<Long, String> nameMap = Map.of();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            nameMap = users.stream().collect(Collectors.toMap(User::getId, u -> u.getRealName() != null ? u.getRealName() : u.getUsername()));
        }

        final Map<Long, String> finalNameMap = nameMap;
        return convs.stream().map(c -> {
            int unread = "STUDENT".equals(role)
                    ? (c.getStudentUnread() != null ? c.getStudentUnread() : 0)
                    : (c.getHrUnread() != null ? c.getHrUnread() : 0);
            Long otherId = "STUDENT".equals(role) ? c.getHrId() : c.getStudentId();
            String otherName = finalNameMap.getOrDefault(otherId, "未知用户");
            return ConversationResponse.builder()
                    .id(c.getId())
                    .studentId(c.getStudentId())
                    .studentName("STUDENT".equals(role) ? null : otherName)
                    .hrId(c.getHrId())
                    .hrName("HR".equals(role) ? null : otherName)
                    .jobId(c.getJobId())
                    .jobTitle(c.getJobTitle())
                    .enterpriseName(c.getEnterpriseName())
                    .lastMessage(c.getLastMessage())
                    .lastMessageAt(c.getLastMessageAt())
                    .unreadCount(unread)
                    .status(c.getStatus())
                    .createdAt(c.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<MessageResponse> getMessages(Long conversationId, Long userId, int page, int size) {
        // 校验权限
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new BusinessException(400, "会话不存在");
        }
        if (!userId.equals(conv.getStudentId()) && !userId.equals(conv.getHrId())) {
            throw new BusinessException(403, "无权查看该会话");
        }

        int offset = Math.max(0, page - 1) * size;
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
               .orderByDesc(ChatMessage::getCreatedAt)
               .last("LIMIT " + offset + ", " + size);

        List<ChatMessage> msgs = chatMessageMapper.selectList(wrapper);
        // 反转回正序
        List<ChatMessage> reversed = new ArrayList<>(msgs);
        java.util.Collections.reverse(reversed);

        return reversed.stream().map(this::toMessageResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long userId, String role) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) return;

        // 重置未读数
        if ("STUDENT".equals(role)) {
            conv.setStudentUnread(0);
        } else {
            conv.setHrUnread(0);
        }
        conversationMapper.updateById(conv);

        // 标记消息已读
        Long otherId = "STUDENT".equals(role) ? conv.getHrId() : conv.getStudentId();
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
               .eq(ChatMessage::getReceiverId, userId)
               .eq(ChatMessage::getIsRead, 0);

        ChatMessage updateMsg = new ChatMessage();
        updateMsg.setIsRead(1);
        updateMsg.setReadAt(LocalDateTime.now());
        chatMessageMapper.update(updateMsg, wrapper);
    }

    @Override
    public int getUnreadCount(Long userId, String role) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        if ("STUDENT".equals(role)) {
            wrapper.eq(Conversation::getStudentId, userId);
        } else {
            wrapper.eq(Conversation::getHrId, userId);
        }
        wrapper.eq(Conversation::getStatus, 1);
        List<Conversation> convs = conversationMapper.selectList(wrapper);

        return convs.stream().mapToInt(c -> {
            int unread = "STUDENT".equals(role)
                    ? (c.getStudentUnread() != null ? c.getStudentUnread() : 0)
                    : (c.getHrUnread() != null ? c.getHrUnread() : 0);
            return unread;
        }).sum();
    }

    @Override
    @Transactional
    public void closeConversation(Long conversationId, Long userId) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new BusinessException(400, "会话不存在");
        }
        if (!userId.equals(conv.getStudentId()) && !userId.equals(conv.getHrId())) {
            throw new BusinessException(403, "无权关闭该会话");
        }
        conv.setStatus(0);
        conversationMapper.updateById(conv);
        log.info("会话已关闭: id={}", conversationId);
    }

    private ConversationResponse toConversationResponse(Conversation c, Integer unread) {
        // 查询双方用户名
        String hrName = null;
        String studentName = null;
        try {
            User hrUser = userMapper.selectById(c.getHrId());
            if (hrUser != null) {
                hrName = hrUser.getRealName() != null ? hrUser.getRealName() : hrUser.getUsername();
            }
            User studentUser = userMapper.selectById(c.getStudentId());
            if (studentUser != null) {
                studentName = studentUser.getRealName() != null ? studentUser.getRealName() : studentUser.getUsername();
            }
        } catch (Exception e) {
            log.warn("查询会话用户名失败", e);
        }
        return ConversationResponse.builder()
                .id(c.getId())
                .studentId(c.getStudentId())
                .studentName(studentName)
                .hrId(c.getHrId())
                .hrName(hrName)
                .jobId(c.getJobId())
                .jobTitle(c.getJobTitle())
                .enterpriseName(c.getEnterpriseName())
                .lastMessage(c.getLastMessage())
                .lastMessageAt(c.getLastMessageAt())
                .unreadCount(unread != null ? unread : 0)
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private MessageResponse toMessageResponse(ChatMessage m) {
        return MessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .senderId(m.getSenderId())
                .senderName(m.getSenderName())
                .senderRole(m.getSenderRole())
                .receiverId(m.getReceiverId())
                .content(m.getContent())
                .msgType(m.getMsgType())
                .fileUrl(m.getFileUrl())
                .isRead(m.getIsRead())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
