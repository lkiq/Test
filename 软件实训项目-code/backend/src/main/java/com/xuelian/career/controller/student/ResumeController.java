package com.xuelian.career.controller.student;

import com.xuelian.career.common.BusinessException;
import com.xuelian.career.common.Result;
import com.xuelian.career.dto.response.ResumeOptimizeResponse;
import com.xuelian.career.entity.ResumeFile;
import com.xuelian.career.mapper.ResumeFileMapper;
import com.xuelian.career.service.ResumeService;
import com.xuelian.career.util.FileUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 简历优化控制器（学生端）
 */
@RestController
@RequestMapping("/api/student/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeFileMapper resumeFileMapper;
    private final FileUtil fileUtil;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /** POST /api/student/resume/upload - 上传简历（自动保存到数据库） */
    @PostMapping("/upload")
    public Result<ResumeFile> upload(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        ResumeFile resumeFile = resumeService.uploadResume(userId, file);
        return Result.success("上传成功", resumeFile);
    }

    /** GET /api/student/resume/list - 获取我的简历列表 */
    @GetMapping("/list")
    public Result<List<ResumeFile>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(resumeService.listResumes(userId));
    }

    /** DELETE /api/student/resume/{id} - 删除简历（同时删除文件和记录） */
    @DeleteMapping("/{id}")
    public Result<String> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        resumeService.deleteResume(userId, id);
        return Result.success("删除成功");
    }

    /** POST /api/student/resume/analyze */
    @PostMapping("/analyze")
    public Result<ResumeOptimizeResponse> analyze(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long targetJobId = body.get("targetJobId") != null ? Long.valueOf(body.get("targetJobId").toString()) : null;
        String fileUrl = (String) body.get("fileUrl");
        return Result.success(resumeService.analyzeResume(userId, targetJobId, fileUrl));
    }

    /** GET /api/student/resume/analysis/{id} */
    @GetMapping("/analysis/{id}")
    public Result<ResumeOptimizeResponse> getAnalysis(@PathVariable Long id) {
        ResumeOptimizeResponse resp = resumeService.getAnalysis(id);
        return resp != null ? Result.success(resp) : Result.notFound("分析记录不存在");
    }

    /** GET /api/student/resume/history */
    @GetMapping("/history")
    public Result<List<ResumeOptimizeResponse>> getHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(resumeService.getHistory(userId));
    }

    /** GET /api/student/resume/{id}/file — 预览/下载自己的简历文件 */
    @GetMapping("/{id}/file")
    public void viewFile(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = (Long) request.getAttribute("userId");

        // 1. 查简历记录并校验归属
        ResumeFile resume = resumeFileMapper.selectById(id);
        if (resume == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(404);
            response.getWriter().write("{\"code\":404,\"message\":\"简历记录不存在\"}");
            return;
        }
        if (!resume.getUserId().equals(userId)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"无权查看该简历\"}");
            return;
        }

        // 2. 检查磁盘文件
        Path filePath = Paths.get(uploadPath, resume.getFileUrl());
        if (!Files.exists(filePath)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(404);
            response.getWriter().write("{\"code\":404,\"message\":\"简历文件已丢失\"}");
            return;
        }

        // 3. 流式返回文件
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
