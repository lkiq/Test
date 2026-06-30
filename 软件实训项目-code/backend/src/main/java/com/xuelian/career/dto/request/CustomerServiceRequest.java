package com.xuelian.career.dto.request;

import lombok.Data;

/**
 * 智能客服聊天请求
 */
@Data
public class CustomerServiceRequest {
    private String question;
    private String userRole;
}
