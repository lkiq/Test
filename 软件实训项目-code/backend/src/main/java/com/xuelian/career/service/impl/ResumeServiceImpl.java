package com.xuelian.career.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuelian.career.common.BusinessException;
import com.xuelian.career.dto.response.ResumeOptimizeResponse;
import com.xuelian.career.entity.JobPosition;
import com.xuelian.career.entity.JobSkillRequirement;
import com.xuelian.career.entity.ResumeAnalysis;
import com.xuelian.career.entity.ResumeFile;
import com.xuelian.career.entity.Skill;
import com.xuelian.career.mapper.JobPositionMapper;
import com.xuelian.career.mapper.JobSkillRequirementMapper;
import com.xuelian.career.mapper.ResumeAnalysisMapper;
import com.xuelian.career.mapper.ResumeFileMapper;
import com.xuelian.career.mapper.SkillMapper;
import com.xuelian.career.service.DeepSeekService;
import com.xuelian.career.service.ResumeService;
import com.xuelian.career.util.FileUtil;
import com.xuelian.career.util.PromptTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 简历优化服务实现 - 文件上传 + 真实文本提取 + AI 结构化分析 + 规则兜底
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeAnalysisMapper analysisMapper;
    private final ResumeFileMapper resumeFileMapper;
    private final JobPositionMapper jobPositionMapper;
    private final JobSkillRequirementMapper jobSkillRequirementMapper;
    private final SkillMapper skillMapper;
    private final DeepSeekService deepSeekService;
    private final PromptTemplateUtil promptUtil;
    private final FileUtil fileUtil;
    private final ObjectMapper objectMapper;

    /** 简历文本过短阈值 */
    private static final int MIN_RESUME_LENGTH = 300;
    /** AI 调用超时：20 秒 */
    private static final long AI_TIMEOUT_MS = 20000L;
    /** AI 最大输出 token */
    private static final int AI_MAX_TOKENS = 2048;
    /** AI 温度参数，结构化输出保持较低温度 */
    private static final double AI_TEMPERATURE = 0.3;
    /** 数字或百分比正则，用于判断成果量化 */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(%|\\s*%|倍|万|千|百|k|w)?");
    /** 常见技能关键词，用于兜底规则 */
    private static final List<String> COMMON_SKILL_KEYWORDS = List.of(
            "java", "python", "spring", "springboot", "mybatis", "mysql", "redis",
            "vue", "react", "javascript", "typescript", "html", "css",
            "linux", "docker", "git", "nginx", "kafka", "rabbitmq",
            "算法", "数据结构", "微服务", "分布式", "前后端分离", "敏捷开发"
    );

    @Override
    @Transactional
    public ResumeFile uploadResume(Long userId, MultipartFile file) {
        // 1. 保存文件到磁盘
        String fileUrl = fileUtil.saveFile(file, "resumes/" + userId);

        // 2. 入库保存上传记录
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }

        ResumeFile resumeFile = new ResumeFile();
        resumeFile.setUserId(userId);
        resumeFile.setFileName(originalName);
        resumeFile.setFileUrl(fileUrl);
        resumeFile.setFileSize(file.getSize());
        resumeFile.setFileType(ext);
        resumeFile.setCreatedAt(LocalDateTime.now());
        resumeFileMapper.insert(resumeFile);

        log.info("简历上传并保存成功: userId={}, file={}, id={}", userId, originalName, resumeFile.getId());
        return resumeFile;
    }

    @Override
    public List<ResumeFile> listResumes(Long userId) {
        return resumeFileMapper.selectList(
                new LambdaQueryWrapper<ResumeFile>()
                        .eq(ResumeFile::getUserId, userId)
                        .orderByDesc(ResumeFile::getCreatedAt));
    }

    @Override
    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        ResumeFile resumeFile = resumeFileMapper.selectById(resumeId);
        if (resumeFile == null) {
            throw new BusinessException("简历记录不存在");
        }
        if (!resumeFile.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该简历");
        }
        // 删除磁盘文件
        fileUtil.deleteFile(resumeFile.getFileUrl());
        // 删除数据库记录
        resumeFileMapper.deleteById(resumeId);
        log.info("简历已删除: userId={}, resumeId={}, file={}", userId, resumeId, resumeFile.getFileUrl());
    }

    @Override
    public ResumeOptimizeResponse analyzeResume(Long userId, Long targetJobId, String fileUrl) {
        // 1. 提取简历真实文本
        String resumeText;
        try {
            resumeText = fileUtil.extractResumeText(fileUrl);
            if (resumeText == null || resumeText.isBlank()) {
                log.warn("简历文本提取为空，进入兜底分析: fileUrl={}", fileUrl);
                return fallbackAnalysis(userId, targetJobId, fileUrl, "");
            }
        } catch (Exception e) {
            log.warn("简历文本提取失败，进入兜底分析: fileUrl={}, error={}", fileUrl, e.getMessage());
            return fallbackAnalysis(userId, targetJobId, fileUrl, "");
        }

        // 2. 查询目标岗位及技能要求
        JobPosition job = targetJobId != null ? jobPositionMapper.selectById(targetJobId) : null;
        String skillRequirements = buildSkillRequirements(targetJobId);

        // 3. 调用 DeepSeek AI 分析
        try {
            if (deepSeekService.isAvailable()) {
                String template = promptUtil.loadTemplate("resume_optimize");
                Map<String, String> params = Map.of(
                        "resume_text", resumeText,
                        "job_jd", job != null ? Optional.ofNullable(job.getJd()).orElse("通用岗位") : "通用岗位",
                        "skill_requirements", skillRequirements
                );
                String prompt = promptUtil.renderTemplate(template, params);

                log.info("开始 AI 简历分析: userId={}, targetJobId={}, fileUrl={}, textLength={}",
                        userId, targetJobId, fileUrl, resumeText.length());
                String response = deepSeekService.callAPI(
                        "你是一位资深的简历优化专家和招聘顾问", prompt,
                        AI_TIMEOUT_MS, AI_MAX_TOKENS, AI_TEMPERATURE);
                Map<String, Object> result = deepSeekService.parseJSONResponse(response);
                if (result != null) {
                    ResumeOptimizeResponse resp = objectMapper.convertValue(result, ResumeOptimizeResponse.class);
                    resp = postProcessResponse(resp, resumeText);
                    saveAnalysis(userId, targetJobId, fileUrl, resp, "AI");
                    log.info("AI 简历分析成功: userId={}, score={}", userId, resp.getScore());
                    return resp;
                }
            }
        } catch (Exception e) {
            log.warn("AI 简历分析失败，使用兜底方案: userId={}, error={}", userId, e.getMessage());
        }

        // 4. 兜底方案
        return fallbackAnalysis(userId, targetJobId, fileUrl, resumeText);
    }

    /**
     * 根据目标岗位 ID 组装技能要求描述
     *
     * @param targetJobId 目标岗位 ID
     * @return 技能要求文本
     */
    private String buildSkillRequirements(Long targetJobId) {
        if (targetJobId == null) {
            return "未指定目标岗位，按通用互联网岗位标准评估";
        }

        List<JobSkillRequirement> requirements = jobSkillRequirementMapper.selectList(
                new LambdaQueryWrapper<JobSkillRequirement>()
                        .eq(JobSkillRequirement::getJobId, targetJobId));
        if (CollectionUtils.isEmpty(requirements)) {
            return "目标岗位暂无明确技能要求，按通用标准评估";
        }

        List<String> parts = new ArrayList<>();
        for (JobSkillRequirement req : requirements) {
            Skill skill = skillMapper.selectById(req.getSkillId());
            if (skill == null) {
                continue;
            }
            String level = req.getRequiredLevel() != null ? req.getRequiredLevel() : "了解";
            parts.add(skill.getName() + "（要求等级：" + level + "）");
        }
        return parts.isEmpty()
                ? "目标岗位暂无明确技能要求，按通用标准评估"
                : String.join("、", parts);
    }

    /**
     * 对 AI 返回结果进行后处理，确保字段完整、评分合理
     *
     * @param resp       AI 返回结果
     * @param resumeText 简历文本
     * @return 处理后的结果
     */
    private ResumeOptimizeResponse postProcessResponse(ResumeOptimizeResponse resp, String resumeText) {
        if (resp == null) {
            resp = new ResumeOptimizeResponse();
        }

        // 综合评分限制在 0-100
        double score = resp.getScore() != null ? resp.getScore() : 70.0;
        resp.setScore(Math.max(0.0, Math.min(100.0, score)));

        // 维度分兜底
        if (resp.getDimensionScores() == null || resp.getDimensionScores().isEmpty()) {
            resp.setDimensionScores(Map.of(
                    "completeness", 70.0,
                    "matching", 70.0,
                    "quantification", 70.0,
                    "expression", 70.0));
        }

        // 问题列表兜底
        if (CollectionUtils.isEmpty(resp.getIssues())) {
            List<ResumeOptimizeResponse.IssueItem> issues = new ArrayList<>();
            if (resumeText.length() < MIN_RESUME_LENGTH) {
                issues.add(buildIssue("高", "完整性", "简历内容较短，信息不够充实",
                        "补充教育背景、项目经历、实习经历、技能清单等关键信息",
                        "XX大学 计算机科学与技术专业 本科 | 2022.09 - 2026.06"));
            }
            if (!NUMBER_PATTERN.matcher(resumeText).find()) {
                issues.add(buildIssue("中", "成果量化", "项目经历缺少量化数据支撑",
                        "使用具体数字描述成果，如性能提升、用户量、访问量、代码量等",
                        "通过 Redis 缓存优化，将接口平均响应时间从 1200ms 降低至 80ms"));
            }
            if (issues.isEmpty()) {
                issues.add(buildIssue("中", "专业表达", "建议进一步突出核心竞争力",
                        "在简历开头增加个人总结，提炼 3-4 项核心技能与优势",
                        "3 年 Java 后端开发经验，熟悉 Spring Boot 微服务架构，主导过日活 10 万级项目"));
            }
            resp.setIssues(issues);
        }

        // 改写片段兜底
        if (CollectionUtils.isEmpty(resp.getOptimizedSnippets())) {
            resp.setOptimizedSnippets(List.of(
                    "建议将项目经历改写为：使用 STAR 法则（背景-任务-行动-结果），并加入量化指标。",
                    "建议在技能清单中突出与目标岗位相关的技术栈，并标注熟练程度。"));
        }

        // 总结兜底
        if (resp.getSummary() == null || resp.getSummary().isBlank()) {
            resp.setSummary("简历整体结构基本完整，建议结合目标岗位要求补充量化成果和突出核心技能。");
        }

        // 完整优化简历兜底
        if (resp.getOptimizedResume() == null || resp.getOptimizedResume().isBlank()) {
            resp.setOptimizedResume(buildOptimizedResume(resumeText, resp.getIssues(), null));
        }

        resp.setSource("AI");
        resp.setCreatedAt(LocalDateTime.now());
        return resp;
    }

    /**
     * 构建一条问题建议项
     */
    private ResumeOptimizeResponse.IssueItem buildIssue(String severity, String category,
                                                        String description, String suggestion,
                                                        String exampleRewrite) {
        return ResumeOptimizeResponse.IssueItem.builder()
                .severity(severity)
                .category(category)
                .description(description)
                .suggestion(suggestion)
                .exampleRewrite(exampleRewrite)
                .build();
    }

    /**
     * 基于简历文本与问题清单生成默认优化简历全文
     * <p>
     * 当 AI 未返回 optimizedResume 或走规则兜底时调用，生成一个可复用的简历框架，
     * 包含个人信息、求职意向、教育背景、技能清单、项目经历 STAR 模板和自我评价。
     * </p>
     *
     * @param resumeText 原始简历文本
     * @param issues     问题建议清单
     * @param job        目标岗位信息（可为空）
     * @return 优化版简历全文
     */
    private String buildOptimizedResume(String resumeText, List<ResumeOptimizeResponse.IssueItem> issues,
                                        JobPosition job) {
        String text = resumeText != null ? resumeText : "";
        boolean hasContent = text.length() >= MIN_RESUME_LENGTH;

        // 从简历中提取已出现的技能关键词
        List<String> matchedSkills = COMMON_SKILL_KEYWORDS.stream()
                .filter(kw -> text.toLowerCase().contains(kw.toLowerCase()))
                .distinct()
                .limit(8)
                .collect(Collectors.toList());

        // 汇总问题建议中的优化方向
        List<String> suggestions = new ArrayList<>();
        if (issues != null) {
            for (ResumeOptimizeResponse.IssueItem issue : issues) {
                if (issue != null && issue.getSuggestion() != null && !issue.getSuggestion().isBlank()) {
                    suggestions.add(issue.getSuggestion());
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【AI 优化版简历】\n\n");

        sb.append("【个人信息 / 求职意向】\n");
        sb.append("姓名：XXX | 电话：XXX | 邮箱：XXX\n");
        if (job != null && job.getTitle() != null) {
            sb.append("求职意向：").append(job.getTitle()).append("\n");
        } else {
            sb.append("求职意向：互联网相关技术岗位\n");
        }
        sb.append("个人总结：建议在此处用 3-4 行提炼核心优势，例如：3 年 Java 后端开发经验，熟悉 Spring Boot 微服务架构，主导过日活 10 万级项目。\n\n");

        sb.append("【教育背景】\n");
        if (hasContent) {
            sb.append("请基于原始简历中的教育信息填写，建议包含：学校、专业、学历、在读/毕业时间、相关课程或荣誉。\n");
        } else {
            sb.append("XX大学 计算机科学与技术 本科（20XX.09 - 20XX.06）\n");
            sb.append("主修课程：数据结构、操作系统、计算机网络、数据库原理\n");
        }
        sb.append("\n");

        sb.append("【技能清单】\n");
        if (!matchedSkills.isEmpty()) {
            sb.append("熟悉/掌握：").append(String.join("、", matchedSkills)).append("\n");
        }
        sb.append("建议根据目标岗位 JD 调整技能顺序，优先展示岗位要求技能，并标注熟练程度。\n\n");

        sb.append("【项目 / 实习经历】（建议按 STAR 法则改写）\n");
        sb.append("项目一：XXX 系统\n");
        sb.append("- 背景（Situation）：简述项目背景与团队角色\n");
        sb.append("- 任务（Task）：明确个人负责的具体模块与目标\n");
        sb.append("- 行动（Action）：使用什么技术、解决了什么问题\n");
        sb.append("- 结果（Result）：用数据说明成果，例如\"接口响应从 1200ms 降至 80ms，QPS 提升 5 倍\"\n\n");

        sb.append("【自我评价】\n");
        sb.append("具备扎实的计算机基础与良好的工程实践能力，熟悉常用开发工具与团队协作流程，对目标岗位方向保持持续学习热情。\n\n");

        if (!suggestions.isEmpty()) {
            sb.append("【本期重点优化方向】\n");
            for (int i = 0; i < suggestions.size(); i++) {
                sb.append(i + 1).append(". ").append(suggestions.get(i)).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 兜底方案：基于简历文本做简单规则分析
     */
    private ResumeOptimizeResponse fallbackAnalysis(Long userId, Long targetJobId,
                                                    String fileUrl, String resumeText) {
        String text = resumeText != null ? resumeText : "";
        int length = text.length();

        // 简单规则评分
        double completenessScore = length < MIN_RESUME_LENGTH ? 55.0 : (length < 800 ? 70.0 : 82.0);
        boolean hasNumber = NUMBER_PATTERN.matcher(text).find();
        double quantificationScore = hasNumber ? 72.0 : 50.0;

        long skillHitCount = COMMON_SKILL_KEYWORDS.stream()
                .filter(kw -> text.toLowerCase().contains(kw.toLowerCase()))
                .count();
        double matchingScore = Math.min(90.0, 55.0 + skillHitCount * 5.0);
        double expressionScore = length < 100 ? 50.0 : 72.0;

        double totalScore = (completenessScore + matchingScore + quantificationScore + expressionScore) / 4.0;

        List<ResumeOptimizeResponse.IssueItem> issues = new ArrayList<>();
        if (length < MIN_RESUME_LENGTH) {
            issues.add(buildIssue("高", "完整性", "简历内容较少，关键信息可能缺失",
                    "补充教育背景、项目经历、实习经历、技能清单、自我评价等模块",
                    "教育背景：XX大学 计算机科学与技术 本科（2022.09 - 2026.06）"));
        }
        if (!hasNumber) {
            issues.add(buildIssue("中", "成果量化", "项目与经历描述缺少量化成果",
                    "在描述项目成果时加入具体数字，如提升效率、降低耗时、用户规模等",
                    "优化数据库索引后，查询耗时从 3 秒降至 200 毫秒，日均查询量 50 万次"));
        }
        if (skillHitCount < 3) {
            issues.add(buildIssue("中", "岗位匹配度", "技能关键词较少，建议突出目标岗位相关技术栈",
                    "根据目标岗位 JD 调整技能清单顺序，优先展示岗位要求的技能",
                    "熟练掌握 Java、Spring Boot、MySQL、Redis，具备微服务设计与开发经验"));
        }
        if (issues.isEmpty()) {
            issues.add(buildIssue("低", "专业表达", "简历基础信息较完整，可进一步精炼表达",
                    "使用更专业、简洁的动词开头描述经历，如主导、设计、实现、优化等",
                    "主导用户模块开发，独立完成订单系统从 0 到 1 的设计与实现"));
        }

        ResumeOptimizeResponse resp = ResumeOptimizeResponse.builder()
                .score((double) Math.round(totalScore))
                .dimensionScores(Map.of(
                        "completeness", completenessScore,
                        "matching", matchingScore,
                        "quantification", quantificationScore,
                        "expression", expressionScore))
                .issues(issues)
                .optimizedSnippets(List.of(
                        "建议将项目经历按 STAR 法则重新组织，突出个人贡献与量化结果。",
                        "建议在简历顶部增加 3-4 行个人总结，快速展示核心竞争力。"))
                .summary("当前 AI 服务不稳定，系统基于简历文本规则给出优化建议。建议补充完整经历、增加量化数据，并突出目标岗位相关技能。")
                .optimizedResume(buildOptimizedResume(text, issues, targetJobId != null ? jobPositionMapper.selectById(targetJobId) : null))
                .source("RULE_FALLBACK")
                .createdAt(LocalDateTime.now())
                .build();

        saveAnalysis(userId, targetJobId, fileUrl, resp, "RULE_FALLBACK");
        return resp;
    }

    @Override
    public ResumeOptimizeResponse getAnalysis(Long analysisId) {
        ResumeAnalysis analysis = analysisMapper.selectById(analysisId);
        if (analysis == null) return null;
        try {
            return objectMapper.readValue(analysis.getResultJson(), ResumeOptimizeResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<ResumeOptimizeResponse> getHistory(Long userId) {
        return analysisMapper.selectList(new LambdaQueryWrapper<ResumeAnalysis>()
                        .eq(ResumeAnalysis::getUserId, userId)
                        .orderByDesc(ResumeAnalysis::getCreatedAt))
                .stream().map(a -> {
                    try {
                        ResumeOptimizeResponse resp = objectMapper.readValue(a.getResultJson(), ResumeOptimizeResponse.class);
                        resp.setId(a.getId());
                        return resp;
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void saveAnalysis(Long userId, Long targetJobId, String fileUrl,
                              ResumeOptimizeResponse resp, String source) {
        try {
            ResumeAnalysis analysis = new ResumeAnalysis();
            analysis.setUserId(userId);
            analysis.setTargetJobId(targetJobId);
            analysis.setFileUrl(fileUrl);
            analysis.setScore(resp.getScore());
            analysis.setResultJson(objectMapper.writeValueAsString(resp));
            analysis.setCreatedAt(LocalDateTime.now());
            analysisMapper.insert(analysis);
        } catch (Exception e) {
            log.warn("保存简历分析记录失败", e);
        }
    }
}
