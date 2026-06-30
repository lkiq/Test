package com.xuelian.career.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuelian.career.common.Result;
import com.xuelian.career.entity.UserFavoriteJob;
import com.xuelian.career.mapper.UserFavoriteJobMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏岗位控制器（学生端）
 */
@RestController
@RequestMapping("/api/student/job")
@RequiredArgsConstructor
public class FavoriteController {

    private final UserFavoriteJobMapper favoriteMapper;

    /**
     * 收藏岗位
     * POST /api/student/job/favorites
     * Body: { "jobId": 1 }
     */
    @PostMapping("/favorites")
    public Result<String> addFavorite(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long jobId = body.get("jobId");

        // 检查是否已收藏
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavoriteJob>()
                .eq(UserFavoriteJob::getUserId, userId)
                .eq(UserFavoriteJob::getJobId, jobId));
        if (count != null && count > 0) {
            return Result.success("已收藏");
        }

        UserFavoriteJob fav = new UserFavoriteJob();
        fav.setUserId(userId);
        fav.setJobId(jobId);
        fav.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(fav);
        return Result.success("收藏成功");
    }

    /**
     * 获取收藏岗位ID列表
     * GET /api/student/job/favorites
     */
    @GetMapping("/favorites")
    public Result<List<Long>> getFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<UserFavoriteJob> list = favoriteMapper.selectList(
                new LambdaQueryWrapper<UserFavoriteJob>()
                        .eq(UserFavoriteJob::getUserId, userId)
                        .orderByDesc(UserFavoriteJob::getCreatedAt));
        List<Long> jobIds = list.stream().map(UserFavoriteJob::getJobId).collect(Collectors.toList());
        return Result.success(jobIds);
    }

    /**
     * 取消收藏
     * DELETE /api/student/job/favorites/{jobId}
     */
    @DeleteMapping("/favorites/{jobId}")
    public Result<String> removeFavorite(@PathVariable Long jobId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteMapper.delete(new LambdaQueryWrapper<UserFavoriteJob>()
                .eq(UserFavoriteJob::getUserId, userId)
                .eq(UserFavoriteJob::getJobId, jobId));
        return Result.success("已取消收藏");
    }

    /**
     * 检查是否收藏
     * GET /api/student/job/favorites/check/{jobId}
     */
    @GetMapping("/favorites/check/{jobId}")
    public Result<Boolean> checkFavorite(@PathVariable Long jobId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavoriteJob>()
                .eq(UserFavoriteJob::getUserId, userId)
                .eq(UserFavoriteJob::getJobId, jobId));
        return Result.success(count != null && count > 0);
    }
}
