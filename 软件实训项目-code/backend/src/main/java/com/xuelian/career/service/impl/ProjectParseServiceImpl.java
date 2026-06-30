package com.xuelian.career.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.dto.response.EnterpriseRecommendResponse.*;
import com.xuelian.career.service.DeepSeekService;
import com.xuelian.career.service.ProjectParseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目需求解析服务实现 - 调用 DeepSeek 解析项目描述为岗位+技能建议
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectParseServiceImpl implements ProjectParseService {

    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper;

    /** 项目解析系统提示词 */
    private static final String PARSE_SYSTEM_PROMPT =
        "你是一位资深技术招聘顾问。请严格根据项目需求描述，分析所需技术岗位和技能要求。" +
        "必须只返回 JSON 格式，不要包含任何其他文字。";

    /** 项目解析用户提示词模板 */
    private static final String PARSE_PROMPT_TEMPLATE =
        "项目需求：%s\n\n" +
        "请以 JSON 格式返回分析结果（严格保持以下结构）：\n" +
        "{\n" +
        "  \"projectSummary\": \"项目一句话概述\",\n" +
        "  \"positions\": [\n" +
        "    {\n" +
        "      \"positionTitle\": \"岗位名称（如 后端开发工程师）\",\n" +
        "      \"skillRequirements\": [\n" +
        "        {\"skillName\": \"技能名称\", \"requiredLevel\": \"精通/熟练/掌握/了解\"}\n" +
        "      ],\n" +
        "      \"headcount\": 需求人数建议（数字）\n" +
        "    }\n" +
        "  ]\n" +
        "}\n\n" +
        "要求：输出2-5个岗位，每个岗位3-8个核心技能，技能等级使用精通/熟练/掌握/了解，技能名称使用行业通用名称。";

    // ---- 兜底关键词匹配表 ----
    private static final List<KeywordRule> FALLBACK_RULES = Arrays.asList(
        new KeywordRule(
            Arrays.asList("商城", "电商", "支付", "订单", "购物车", "商品"),
            Arrays.asList(
                pos("后端开发工程师", java("精通", "Spring Boot", "熟练", "MySQL", "熟练", "Redis", "掌握", "微服务", "了解"), 3),
                pos("前端开发工程师", skills("Vue", "熟练", "React", "掌握", "JavaScript", "精通", "CSS", "熟练", "Element Plus", "掌握"), 2),
                pos("测试工程师", skills("JUnit", "掌握", "Selenium", "掌握", "Postman", "熟练", "性能测试", "了解"), 1)
            )
        ),
        new KeywordRule(
            Arrays.asList("数据", "报表", "分析", "可视化", "数仓", "大屏"),
            Arrays.asList(
                pos("数据分析师", skills("Python", "熟练", "SQL", "精通", "Pandas", "掌握", "ECharts", "掌握", "Spark", "了解"), 2),
                pos("后端开发工程师", java("熟练", "Spring Boot", "熟练", "MySQL", "精通", "Redis", "掌握"), 2)
            )
        ),
        new KeywordRule(
            Arrays.asList("APP", "移动", "Android", "iOS", "小程序", "Flutter", "React Native"),
            Arrays.asList(
                pos("移动端开发工程师", skills("Flutter", "熟练", "Dart", "掌握", "Java/Kotlin", "掌握", "Swift", "了解", "RESTful API", "掌握"), 2),
                pos("后端开发工程师", java("熟练", "Spring Boot", "熟练", "MySQL", "熟练"), 1)
            )
        ),
        new KeywordRule(
            Arrays.asList("后台", "管理", "CRUD", "审批", "权限", "RBAC"),
            Arrays.asList(
                pos("后端开发工程师", java("精通", "Spring Boot", "精通", "MySQL", "熟练", "Redis", "掌握", "Spring Security", "掌握"), 2),
                pos("前端开发工程师", skills("Vue", "熟练", "Element Plus", "熟练", "TypeScript", "掌握"), 1)
            )
        ),
        new KeywordRule(
            Arrays.asList("推荐", "算法", "机器学习", "AI", "NLP", "深度学习", "神经网络"),
            Arrays.asList(
                pos("算法工程师", skills("Python", "精通", "TensorFlow", "熟练", "PyTorch", "熟练", "机器学习", "精通", "深度学习", "掌握"), 2),
                pos("后端开发工程师", java("熟练", "Spring Boot", "熟练", "MySQL", "掌握"), 1)
            )
        ),
        new KeywordRule(
            Arrays.asList("直播", "视频", "IM", "WebRTC", "实时", "流媒体"),
            Arrays.asList(
                pos("后端开发工程师", skills("Java", "精通", "Netty", "熟练", "WebSocket", "掌握", "Spring Boot", "熟练", "Redis", "掌握"), 2),
                pos("前端开发工程师", skills("Vue", "熟练", "WebRTC", "掌握", "WebSocket", "掌握"), 1)
            )
        ),
        new KeywordRule(
            Arrays.asList("测试", "质量", "自动化", "CI/CD"),
            Arrays.asList(
                pos("测试工程师", skills("Selenium", "熟练", "JUnit", "掌握", "JMeter", "掌握", "Jenkins", "了解", "Docker", "了解"), 2)
            )
        ),
        new KeywordRule(
            Arrays.asList("运营", "活动", "营销", "用户增长", "新媒体"),
            Arrays.asList(
                pos("运营专员", skills("SQL", "掌握", "Excel", "精通", "Python", "了解", "数据分析", "掌握", "文案策划", "熟练"), 2)
            )
        )
    );

    @Override
    public ParseResult parseProject(String projectDescription) {
        // 尝试 AI 解析
        try {
            if (deepSeekService.isAvailable()) {
                String prompt = String.format(PARSE_PROMPT_TEMPLATE, projectDescription);
                String response = deepSeekService.callAPI(PARSE_SYSTEM_PROMPT, prompt);
                Map<String, Object> parsed = deepSeekService.parseJSONResponse(response);
                if (parsed != null && parsed.containsKey("positions")) {
                    ParseResult result = buildFromMap(parsed);
                    log.info("AI 项目解析成功: positions={}", result.getPositions().size());
                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("AI 项目解析失败，降级到关键词匹配: {}", e.getMessage());
        }

        // 兜底：关键词匹配
        return fallbackParse(projectDescription);
    }

    /**
     * 兜底关键词匹配解析
     */
    private ParseResult fallbackParse(String text) {
        String lower = text.toLowerCase();
        List<PositionSuggestion> bestPositions = null;
        int bestScore = 0;

        for (KeywordRule rule : FALLBACK_RULES) {
            int score = 0;
            for (String kw : rule.keywords) {
                if (lower.contains(kw.toLowerCase())) score++;
            }
            if (score > bestScore) {
                bestScore = score;
                bestPositions = rule.positions;
            }
        }

        String summary;
        if (bestPositions == null || bestPositions.isEmpty()) {
            // 默认：通用后端+前端
            bestPositions = Arrays.asList(
                pos("后端开发工程师", skills("Java", "精通", "Spring Boot", "熟练", "MySQL", "熟练", "Redis", "掌握", "Linux", "了解"), 2),
                pos("前端开发工程师", skills("Vue", "熟练", "JavaScript", "掌握", "HTML/CSS", "熟练", "TypeScript", "了解"), 1)
            );
            summary = "通用软件项目开发";
        } else {
            summary = text.substring(0, Math.min(80, text.length()));
        }

        log.info("兜底关键词解析完成: score={}, positions={}", bestScore, bestPositions.size());
        return new ParseResult(summary, bestPositions);
    }

    @SuppressWarnings("unchecked")
    private ParseResult buildFromMap(Map<String, Object> map) {
        String summary = (String) map.getOrDefault("projectSummary", "");
        List<Map<String, Object>> posList = (List<Map<String, Object>>) map.get("positions");
        List<PositionSuggestion> positions = new ArrayList<>();

        if (posList != null) {
            for (Map<String, Object> pm : posList) {
                PositionSuggestion pos = PositionSuggestion.builder()
                    .positionTitle((String) pm.get("positionTitle"))
                    .headcount(pm.get("headcount") instanceof Number ? ((Number) pm.get("headcount")).intValue() : 1)
                    .skillRequirements(new ArrayList<>())
                    .build();

                List<Map<String, Object>> skills = (List<Map<String, Object>>) pm.get("skillRequirements");
                if (skills != null) {
                    for (Map<String, Object> sm : skills) {
                        pos.getSkillRequirements().add(SkillRequirement.builder()
                            .skillName((String) sm.get("skillName"))
                            .requiredLevel((String) sm.get("requiredLevel"))
                            .build());
                    }
                }
                positions.add(pos);
            }
        }

        return new ParseResult(summary, positions);
    }

    // ---- 便捷构造方法 ----
    private static PositionSuggestion pos(String title, List<SkillRequirement> skills, int headcount) {
        return PositionSuggestion.builder()
            .positionTitle(title)
            .skillRequirements(skills)
            .headcount(headcount)
            .build();
    }

    private static List<SkillRequirement> skills(String... nameLevelPairs) {
        List<SkillRequirement> list = new ArrayList<>();
        for (int i = 0; i < nameLevelPairs.length; i += 2) {
            list.add(SkillRequirement.builder()
                .skillName(nameLevelPairs[i])
                .requiredLevel(nameLevelPairs[i + 1])
                .build());
        }
        return list;
    }

    private static List<SkillRequirement> java(String level, String... rest) {
        List<SkillRequirement> list = new ArrayList<>();
        list.add(SkillRequirement.builder().skillName("Java").requiredLevel(level).build());
        list.addAll(skills(rest));
        return list;
    }

    /** 兜底关键词规则 */
    private static class KeywordRule {
        final List<String> keywords;
        final List<PositionSuggestion> positions;
        KeywordRule(List<String> keywords, List<PositionSuggestion> positions) {
            this.keywords = keywords;
            this.positions = positions;
        }
    }
}
