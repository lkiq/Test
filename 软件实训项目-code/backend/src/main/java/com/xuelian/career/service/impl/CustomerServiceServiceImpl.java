package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.dto.response.ChatResponse;
import com.xuelian.career.entity.Faq;
import com.xuelian.career.mapper.FaqMapper;
import com.xuelian.career.service.CustomerServiceService;
import com.xuelian.career.service.DeepSeekService;
import com.xuelian.career.util.PromptTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能客服服务实现 - FAQ 关键词匹配优先 + DeepSeek AI 兜底
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceServiceImpl implements CustomerServiceService {

    private final FaqMapper faqMapper;
    private final DeepSeekService deepSeekService;
    private final PromptTemplateUtil promptUtil;

    @Override
    public ChatResponse chat(String question, String userRole) {
        // 1. FAQ 关键词匹配（最快路径）
        List<Faq> allFaqs;
        try {
            allFaqs = faqMapper.selectList(new LambdaQueryWrapper<Faq>().eq(Faq::getIsDeleted, 0));
        } catch (Exception e) {
            log.warn("FAQ 查询失败，跳过 FAQ 匹配: {}", e.getMessage());
            allFaqs = List.of();
        }

        Faq bestMatch = findBestMatch(question, allFaqs);

        if (bestMatch != null) {
            List<String> related = allFaqs.stream()
                    .filter(f -> !f.getId().equals(bestMatch.getId()) && f.getCategory().equals(bestMatch.getCategory()))
                    .limit(3).map(Faq::getQuestion).collect(Collectors.toList());
            return ChatResponse.builder().answer(bestMatch.getAnswer()).source("FAQ").relatedQuestions(related).build();
        }

        // 2. DeepSeek AI 智能回答（带 FAQ 知识库上下文）
        try {
            if (deepSeekService.isAvailable()) {
                ChatResponse aiResp = callAIChat(question, userRole, allFaqs);
                if (aiResp != null) {
                    return aiResp;
                }
            }
        } catch (Exception e) {
            log.warn("DeepSeek 客服回答失败，使用兜底回答: {}", e.getMessage());
        }

        // 3. 最终兜底回答
        return ChatResponse.builder()
                .answer("抱歉，我暂时无法回答您的问题。您可以尝试以下方式：\n1. 换个方式提问\n2. 查看左侧常见问题列表\n3. 联系平台管理员获取帮助")
                .source("FALLBACK")
                .relatedQuestions(List.of("如何使用平台？", "平台有哪些功能？", "如何联系管理员？"))
                .build();
    }

    @Override
    public List<Faq> getFAQs() {
        return faqMapper.selectList(new LambdaQueryWrapper<Faq>()
                .eq(Faq::getIsDeleted, 0).orderByAsc(Faq::getCategory).orderByAsc(Faq::getSortOrder));
    }

    /**
     * 调用 DeepSeek AI 进行智能客服回答
     */
    private ChatResponse callAIChat(String question, String userRole, List<Faq> allFaqs) {
        try {
            // 构建 FAQ 候选上下文（带类别标记）
            StringBuilder faqContext = new StringBuilder();
            for (Faq faq : allFaqs) {
                faqContext.append("- [").append(faq.getCategory()).append("] ");
                faqContext.append("问: ").append(faq.getQuestion()).append("\n");
                faqContext.append("  答: ").append(faq.getAnswer()).append("\n");
            }

            Map<String, String> params = new HashMap<>();
            params.put("user_role", userRole != null ? userRole : "STUDENT");
            params.put("faq_candidates", faqContext.toString());
            params.put("question", question != null ? question : "");

            String prompt = promptUtil.loadAndRender("customer_service", params);
            String systemPrompt = "你是一位求职辅导平台的智能客服助手，请严格按照JSON格式返回回答。";

            // 使用缓存（相同问题+角色不重复请求）
            // cacheKey 用 question 的 SHA-256，避免 Objects.hash(int) 冲突导致不同问题命中同一缓存
            String cacheKey = "cs:" + userRole + ":" + sha256Hex(question != null ? question : "");
            String response = deepSeekService.callAPIWithCache(cacheKey, systemPrompt, prompt, 3600L, 512);
            Map<String, Object> result = deepSeekService.parseJSONResponse(response);

            if (result != null && result.containsKey("answer")) {
                @SuppressWarnings("unchecked")
                List<String> related = (List<String>) result.getOrDefault("relatedQuestions", List.of());
                return ChatResponse.builder()
                        .answer((String) result.get("answer"))
                        .source((String) result.getOrDefault("source", "AI"))
                        .relatedQuestions(related)
                        .build();
            }

            // JSON 解析失败，直接用原始文本
            return ChatResponse.builder()
                    .answer(response != null ? response : "AI 暂时无法回答")
                    .source("AI")
                    .relatedQuestions(List.of())
                    .build();

        } catch (Exception e) {
            log.warn("DeepSeek 客服调用异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 关键词匹配：按命中关键词数量 + 分类权重排序
     */
    private Faq findBestMatch(String question, List<Faq> faqs) {
        if (question == null || question.isEmpty()) return null;
        for (Faq faq : faqs) {
            if (faq.getKeywords() != null) {
                String[] keywords = faq.getKeywords().split(",");
                for (String kw : keywords) {
                    if (question.contains(kw.trim())) return faq;
                }
            }
        }
        // 模糊匹配：按问题文本包含度
        for (Faq faq : faqs) {
            String q = faq.getQuestion();
            if (q != null && q.length() >= 10 && question.contains(q.substring(0, 10)))
                return faq;
        }
        return null;
    }

    /**
     * 计算字符串的 SHA-256 十六进制摘要（用于生成唯一缓存键）
     * @param input 输入字符串
     * @return 64 位十六进制哈希字符串
     */
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
