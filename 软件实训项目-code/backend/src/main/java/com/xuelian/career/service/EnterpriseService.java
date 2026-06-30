package com.xuelian.career.service;

import com.xuelian.career.dto.request.EnterpriseRecommendRequest;
import com.xuelian.career.dto.response.EnterpriseRecommendResponse;
import java.util.List;

/**
 * 企业推荐服务接口
 */
public interface EnterpriseService {
    EnterpriseRecommendResponse recommend(Long userId, EnterpriseRecommendRequest request);
    List<EnterpriseRecommendResponse> getHistory(Long userId);
}
