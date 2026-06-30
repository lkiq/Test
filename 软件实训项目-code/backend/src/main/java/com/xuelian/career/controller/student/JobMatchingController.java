package com.xuelian.career.controller.student;

import com.xuelian.career.common.Result;
import com.xuelian.career.dto.response.JobMatchResponse;
import com.xuelian.career.entity.JobPosition;
import com.xuelian.career.service.JobMatchingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位匹配推荐控制器（学生端）
 */
@RestController
@RequestMapping("/api/student/jobs")
@RequiredArgsConstructor
public class JobMatchingController {

    private final JobMatchingService jobMatchingService;

    /** POST /api/student/jobs/recommend */
    @PostMapping("/recommend")
    public Result<List<JobMatchResponse>> recommend(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(jobMatchingService.recommendJobs(userId));
    }

    /** GET /api/student/jobs/{id} */
    @GetMapping("/{id}")
    public Result<JobPosition> getDetail(@PathVariable Long id) {
        JobPosition job = jobMatchingService.getJobDetail(id);
        return job != null ? Result.success(job) : Result.notFound("岗位不存在");
    }

    /** GET /api/student/jobs/search?keyword=&city= */
    @GetMapping("/search")
    public Result<List<JobPosition>> search(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String city) {
        return Result.success(jobMatchingService.searchJobs(keyword, city));
    }
}
