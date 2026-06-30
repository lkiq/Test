package com.xuelian.career.util;

import com.xuelian.career.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt 模板工具类 - 从 classpath:prompts/ 加载模板文件并替换参数占位符
 * 模板中使用 {{placeholder}} 表示可替换参数
 */
@Slf4j
@Component
public class PromptTemplateUtil {

    /** 模板缓存，避免重复读取文件 */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /**
     * 加载指定名称的 Prompt 模板
     * @param templateName 模板文件名（不含 .txt 后缀，如 "career_exploration"）
     * @return 模板内容字符串
     */
    public String loadTemplate(String templateName) {
        return cache.computeIfAbsent(templateName, name -> {
            try {
                String path = "prompts/" + name + ".txt";
                ClassPathResource resource = new ClassPathResource(path);
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                log.debug("Prompt 模板加载成功: {}", path);
                return sb.toString().trim();
            } catch (IOException e) {
                log.error("Prompt 模板加载失败: {}", templateName, e);
                throw new BusinessException("AI 模板加载失败");
            }
        });
    }

    /**
     * 渲染模板 - 将模板中的 {{placeholder}} 替换为实际参数值
     * @param template 模板内容
     * @param params   参数映射表（key -> value）
     * @return 渲染后的 Prompt 内容
     */
    public String renderTemplate(String template, Map<String, String> params) {
        String result = template;
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue() : "";
                result = result.replace(placeholder, value);
            }
        }
        return result;
    }

    /**
     * 加载并渲染模板（一步完成）
     * @param templateName 模板名
     * @param params       参数
     * @return 渲染后的 Prompt
     */
    public String loadAndRender(String templateName, Map<String, String> params) {
        String template = loadTemplate(templateName);
        return renderTemplate(template, params);
    }

    /**
     * 清除模板缓存（用于模板热更新场景）
     */
    public void clearCache() {
        cache.clear();
    }
}
