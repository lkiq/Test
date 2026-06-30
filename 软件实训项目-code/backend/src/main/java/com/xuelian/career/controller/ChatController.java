package com.xuelian.career.controller;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.request.CreateConversationRequest;
import com.xuelian.career.dto.request.SendMessageRequest;
import com.xuelian.career.dto.response.ConversationResponse;
import com.xuelian.career.dto.response.MessageResponse;
import com.xuelian.career.dto.response.UnreadCountResponse;
import com.xuelian.career.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天控制器 - 学生与HR站内信功能
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** 创建或获取会话 */
    @PostMapping("/conversations")
    public Result<ConversationResponse> createConversation(@RequestBody CreateConversationRequest request,
                                                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        if (!"STUDENT".equals(role)) {
            return Result.forbidden("仅学生可以发起聊天");
        }
        ConversationResponse resp = chatService.createOrGetConversation(
                userId, request.getHrId(), request.getJobId(),
                request.getJobTitle(), request.getEnterpriseName());
        return Result.success(resp);
    }

    /** 发送消息 */
    @PostMapping("/messages")
    public Result<MessageResponse> sendMessage(@RequestBody SendMessageRequest request,
                                                HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        String username = (String) httpRequest.getAttribute("username");
        MessageResponse resp = chatService.sendMessage(
                request.getConversationId(), userId, role, username,
                request.getContent(), request.getMsgType());
        return Result.success(resp);
    }

    /** 获取会话列表 */
    @GetMapping("/conversations")
    public Result<List<ConversationResponse>> getConversations(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        return Result.success(chatService.getConversations(userId, role));
    }

    /** 获取会话历史消息 */
    @GetMapping("/conversations/{id}/messages")
    public Result<List<MessageResponse>> getMessages(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "50") int size,
                                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(chatService.getMessages(id, userId, page, size));
    }

    /** 标记会话已读 */
    @PutMapping("/conversations/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        chatService.markAsRead(id, userId, role);
        return Result.success();
    }

    /** 获取未读消息总数 */
    @GetMapping("/unread-count")
    public Result<UnreadCountResponse> getUnreadCount(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        int count = chatService.getUnreadCount(userId, role);
        return Result.success(UnreadCountResponse.builder().total(count).build());
    }

    /** 关闭会话 */
    @PutMapping("/conversations/{id}/close")
    public Result<Void> closeConversation(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        chatService.closeConversation(id, userId);
        return Result.success();
    }
}
