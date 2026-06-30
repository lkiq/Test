package com.xuelian.career.service;

import com.xuelian.career.dto.response.ChatResponse;
import com.xuelian.career.entity.Faq;
import java.util.List;

/**
 * 智能客服服务接口
 */
public interface CustomerServiceService {
    ChatResponse chat(String question, String userRole);
    List<Faq> getFAQs();
}
