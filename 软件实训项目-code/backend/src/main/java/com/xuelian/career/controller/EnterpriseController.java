package com.xuelian.career.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.common.BusinessException;
import com.xuelian.career.common.Result;
import com.xuelian.career.dto.request.EnterpriseRecommendRequest;
import com.xuelian.career.dto.response.EnterpriseRecommendResponse;
import com.xuelian.career.entity.JobApplication;
import com.xuelian.career.entity.JobPosition;
import com.xuelian.career.entity.ResumeFile;
import com.xuelian.career.service.EnterpriseService;
import com.xuelian.career.service.JobApplicationService;
import com.xuelian.career.service.JobPublishService;
import com.xuelian.career.util.FileUtil;
import com.xuelian.career.mapper.JobApplicationMapper;
import com.xuelian.career.mapper.JobPositionMapper;
import com.xuelian.career.mapper.ResumeFileMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企业端控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;
    private final JobPublishService jobPublishService;
    private final JobApplicationService applicationService;
    private final FileUtil fileUtil;
    private final JobApplicationMapper applicationMapper;
    private final JobPositionMapper jobPositionMapper;
    private final ResumeFileMapper resumeFileMapper;

    /** GET /api/enterprise/dashboard — 企业首页统计 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        // 获取所有岗位
        List<JobPosition> jobs = jobPositionMapper.selectList(
                new LambdaQueryWrapper<JobPosition>()
                        .eq(JobPosition::getHrUserId, userId)
                        .eq(JobPosition::getIsDeleted, 0));

        int totalJobs = jobs.size();
        int activeJobs = (int) jobs.stream().filter(j -> j.getPublishStatus() != null && j.getPublishStatus() == 1).count();
        int offlineJobs = totalJobs - activeJobs;

        // 获取该HR所有岗位的ID
        List<Long> jobIds = jobs.stream().map(JobPosition::getId).collect(Collectors.toList());

        int totalCandidates = 0;
        int pendingCandidates = 0;
        int interviewCandidates = 0;
        int rejectedCandidates = 0;

        if (!jobIds.isEmpty()) {
            List<JobApplication> applications = applicationMapper.selectList(
                    new LambdaQueryWrapper<JobApplication>()
                            .in(JobApplication::getJobId, jobIds));

            totalCandidates = applications.size();
            pendingCandidates = (int) applications.stream()
                    .filter(a -> "PENDING".equals(a.getStatus())).count();
            interviewCandidates = (int) applications.stream()
                    .filter(a -> "INTERVIEW".equals(a.getStatus())).count();
            rejectedCandidates = (int) applications.stream()
                    .filter(a -> "REJECT".equals(a.getStatus())).count();
        }

        // 最近发布的岗位（前3个）
        List<Map<String, Object>> recentJobs = jobs.stream()
                .sorted(Comparator.comparing(JobPosition::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(j -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", j.getId());
                    m.put("title", j.getTitle());
                    m.put("city", j.getCity());
                    m.put("salaryRange", j.getSalaryRange());
                    m.put("publishStatus", j.getPublishStatus());
                    m.put("publishTime", j.getPublishTime());
                    m.put("logoUrl", j.getLogoUrl());
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalJobs", totalJobs);
        result.put("activeJobs", activeJobs);
        result.put("offlineJobs", offlineJobs);
        result.put("totalCandidates", totalCandidates);
        result.put("pendingCandidates", pendingCandidates);
        result.put("interviewCandidates", interviewCandidates);
        result.put("rejectedCandidates", rejectedCandidates);
        result.put("recentJobs", recentJobs);

        return Result.success(result);
    }

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /** POST /api/enterprise/recommend */
    @PostMapping("/recommend")
    public Result<EnterpriseRecommendResponse> recommend(@RequestBody EnterpriseRecommendRequest request,
                                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (request.getProjectDescription() == null || request.getProjectDescription().length() < 20) {
            return Result.badRequest("项目描述至少需要20个字符");
        }
        return Result.success(enterpriseService.recommend(userId, request));
    }

    /** GET /api/enterprise/recommend/history */
    @GetMapping("/recommend/history")
    public Result<List<EnterpriseRecommendResponse>> getHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(enterpriseService.getHistory(userId));
    }

    /** POST /api/enterprise/recommend/pdf — 上传 PDF 项目需求文档，AI 解析后推荐 */
    @PostMapping("/recommend/pdf")
    public Result<EnterpriseRecommendResponse> recommendByPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "education", required = false) String education,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "skillPreference", required = false) String skillPreference,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 文件类型校验
        if (file.isEmpty()) {
            return Result.badRequest("请上传 PDF 文件");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            return Result.badRequest("仅支持 PDF 格式文件");
        }

        // PDF 文本提取
        String extractedText;
        try {
            extractedText = extractPdfText(file);
        } catch (Exception e) {
            log.error("PDF 文本提取失败: {}", e.getMessage());
            return Result.badRequest("PDF 文件解析失败：" + e.getMessage());
        }

        if (extractedText == null || extractedText.trim().length() < 20) {
            return Result.badRequest("PDF 内容不足，无法提取足够的项目需求信息（至少需要20字符）");
        }

        // 构建请求并调用推荐
        EnterpriseRecommendRequest req = new EnterpriseRecommendRequest();
        req.setProjectDescription(extractedText.trim());

        if (education != null || city != null || skillPreference != null) {
            EnterpriseRecommendRequest.RecommendFilters filters =
                    new EnterpriseRecommendRequest.RecommendFilters();
            filters.setEducation(education);
            filters.setCity(city);
            filters.setSkillPreference(skillPreference);
            req.setFilters(filters);
        }

        log.info("PDF 推荐请求: fileName={}, 提取文本长度={}", originalName, extractedText.length());
        return Result.success(enterpriseService.recommend(userId, req));
    }

    /** 使用 PDFBox 提取 PDF 文本内容 */
    private String extractPdfText(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getBytes());
             StringWriter sw = new StringWriter()) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setLineSeparator("\n");
            String text = stripper.getText(document);
            // 清理多余空行
            text = text.replaceAll("\\n{3,}", "\n\n").trim();
            // 限制最长 8000 字符（DeepSeek API 限制）
            if (text.length() > 8000) {
                log.info("PDF 文本过长（{}字符），截取前8000字符", text.length());
                text = text.substring(0, 8000);
            }
            return text;
        }
    }

    // ===================== 岗位管理 =====================

    /** POST /api/enterprise/jobs — 发布新岗位 */
    @PostMapping("/jobs")
    public Result<JobPosition> publishJob(@RequestBody JobPosition job, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            return Result.badRequest("岗位名称不能为空");
        }
        if (job.getCompanyName() == null || job.getCompanyName().trim().isEmpty()) {
            return Result.badRequest("公司名称不能为空");
        }
        return Result.success("岗位发布成功", jobPublishService.publishJob(userId, job));
    }

    /** GET /api/enterprise/jobs — 获取我发布的岗位列表 */
    @GetMapping("/jobs")
    public Result<List<JobPosition>> listMyJobs(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(jobPublishService.listMyJobs(userId));
    }

    /** PUT /api/enterprise/jobs/{id} — 更新岗位 */
    @PutMapping("/jobs/{id}")
    public Result<JobPosition> updateJob(@PathVariable Long id, @RequestBody JobPosition job,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("岗位更新成功", jobPublishService.updateJob(userId, id, job));
    }

    /** PUT /api/enterprise/jobs/{id}/offline — 下架岗位 */
    @PutMapping("/jobs/{id}/offline")
    public Result<String> offlineJob(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        jobPublishService.offlineJob(userId, id);
        return Result.success("岗位已下架");
    }

    /** PUT /api/enterprise/jobs/{id}/republish — 重新发布岗位 */
    @PutMapping("/jobs/{id}/republish")
    public Result<String> republishJob(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        jobPublishService.republishJob(userId, id);
        return Result.success("岗位已重新发布");
    }

    /** DELETE /api/enterprise/jobs/{id} — 删除已下架的岗位 */
    @DeleteMapping("/jobs/{id}")
    public Result<String> deleteJob(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        jobPublishService.deleteJob(userId, id);
        return Result.success("岗位已删除");
    }

    /** POST /api/enterprise/jobs/upload-image — 上传岗位图片/Logo */
    @PostMapping("/jobs/upload-image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String relativePath = fileUtil.saveImageFile(file, "images");
        String url = fileUtil.getFileUrl(relativePath);
        return Result.success("图片上传成功", url);
    }

    // ===================== 投递管理 =====================

    /** GET /api/enterprise/jobs/{id}/applications — 查看某岗位的投递列表 */
    @GetMapping("/jobs/{id}/applications")
    public Result<List<Map<String, Object>>> listApplications(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(applicationService.listJobApplications(userId, id));
    }

    /** DELETE /api/enterprise/applications/{id} — 删除投递记录 */
    @DeleteMapping("/applications/{id}")
    public Result<String> deleteApplication(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        applicationService.deleteApplication(userId, id);
        return Result.success("已删除投递记录");
    }

    /** PUT /api/enterprise/applications/{id}/status — 更新投递状态（邀约/不合适） */
    @PutMapping("/applications/{id}/status")
    public Result<?> updateApplicationStatus(@PathVariable Long id, @RequestBody Map<String, String> body,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String status = body.get("status");
        if (status == null || status.isEmpty()) {
            return Result.badRequest("状态不能为空");
        }
        return Result.success(applicationService.updateStatus(userId, id, status));
    }

    /** GET /api/enterprise/applications/{id}/resume — 下载投递人的简历文件 */
    @GetMapping("/applications/{id}/resume")
    public void downloadResume(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long hrUserId = (Long) request.getAttribute("userId");

        // 1. 查投递记录
        JobApplication app = applicationMapper.selectById(id);
        if (app == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(404);
            response.getWriter().write("{\"code\":404,\"message\":\"投递记录不存在\"}");
            return;
        }

        // 2. 检验岗位归属（通过 service 已有校验逻辑）
        try {
            applicationService.listJobApplications(hrUserId, app.getJobId());
        } catch (BusinessException e) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"无权查看该简历\"}");
            return;
        }

        // 3. 查简历文件
        if (app.getResumeId() == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(404);
            response.getWriter().write("{\"code\":404,\"message\":\"该申请人未上传简历\"}");
            return;
        }
        ResumeFile resume = resumeFileMapper.selectById(app.getResumeId());
        if (resume == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(404);
            response.getWriter().write("{\"code\":404,\"message\":\"简历文件不存在\"}");
            return;
        }

        // 4. 读取磁盘文件并流式返回
        Path filePath = Paths.get(uploadPath, resume.getFileUrl());
        if (!Files.exists(filePath)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(404);
            response.getWriter().write("{\"code\":404,\"message\":\"简历文件已丢失\"}");
            return;
        }

        String contentType = "application/pdf";
        if ("docx".equalsIgnoreCase(resume.getFileType())) {
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition",
                "inline; filename*=UTF-8''" + URLEncoder.encode(resume.getFileName(), StandardCharsets.UTF_8));
        response.setContentLengthLong(resume.getFileSize());

        try (OutputStream out = response.getOutputStream()) {
            Files.copy(filePath, out);
            out.flush();
        }
    }
}
