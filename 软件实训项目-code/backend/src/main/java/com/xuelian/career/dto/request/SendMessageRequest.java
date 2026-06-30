package com.xuelian.career.dto.request;

import lombok.Data;

/**
 * 发送消息请求
 */
@Data
public class SendMessageRequest {
    /** 会话ID */
    private Long conversationId;
    /** 消息内容 */
    private String content;
    /** 消息类型: TEXT/IMAGE/FILE */
    private String msgType;
}
