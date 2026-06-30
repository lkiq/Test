package com.xuelian.career.service;

import com.xuelian.career.dto.response.ResumeOptimizeResponse;
import com.xuelian.career.entity.ResumeFile;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 简历优化服务接口
 */
public interface ResumeService {
    ResumeFile uploadResume(Long userId, MultipartFile file);
    ResumeOptimizeResponse analyzeResume(Long userId, Long targetJobId, String fileUrl);
    ResumeOptimizeResponse getAnalysis(Long analysisId);
    List<ResumeOptimizeResponse> getHistory(Long userId);
    List<ResumeFile> listResumes(Long userId);
    void deleteResume(Long userId, Long resumeId);
}
