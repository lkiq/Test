package com.xuelian.career.controller;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.request.CustomerServiceRequest;
import com.xuelian.career.dto.response.ChatResponse;
import com.xuelian.career.entity.Faq;
import com.xuelian.career.service.CustomerServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能客服控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/customer-service")
@RequiredArgsConstructor
public class CustomerServiceController {

    private final CustomerServiceService customerServiceService;

    /** POST /api/customer-service/chat */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody CustomerServiceRequest request) {
        try {
            ChatResponse response = customerServiceService.chat(request.getQuestion(), request.getUserRole());
            return Result.success(response);
        } catch (Exception e) {
            log.error("智能客服异常: question={}, error={}", request.getQuestion(), e.getMessage(), e);
            // 返回兜底响应（code=200），避免前端进入错误处理
            return Result.success(ChatResponse.builder()
                    .answer("抱歉，客服服务暂时不可用。您可以尝试：\n1. 换个方式提问\n2. 查看左侧常见问题\n3. 稍后重试")
                    .source("ERROR")
                    .build());
        }
    }

    /** GET /api/customer-service/faqs */
    @GetMapping("/faqs")
    public Result<List<Faq>> getFAQs() {
        return Result.success(customerServiceService.getFAQs());
    }
}
