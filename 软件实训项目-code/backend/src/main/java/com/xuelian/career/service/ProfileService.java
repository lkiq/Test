package com.xuelian.career.service;

import com.xuelian.career.dto.request.ProfileRequest;
import com.xuelian.career.dto.response.ProfileResponse;

/**
 * 求职画像服务接口
 */
public interface ProfileService {

    /**
     * 保存或更新求职画像
     * @param userId  用户ID
     * @param request 画像请求
     * @return 保存后的画像
     */
    ProfileResponse saveProfile(Long userId, ProfileRequest request);

    /**
     * 获取当前用户的求职画像
     * @param userId 用户ID
     * @return 画像信息，不存在返回 null
     */
    ProfileResponse getProfile(Long userId);
}
