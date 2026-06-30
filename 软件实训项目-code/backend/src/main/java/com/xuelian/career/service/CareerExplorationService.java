package com.xuelian.career.service;

import com.xuelian.career.dto.request.CareerExplorationRequest;
import com.xuelian.career.dto.response.CareerDirectionResponse;
import java.util.List;

/**
 * AI职业方向探索服务接口
 */
public interface CareerExplorationService {
    CareerDirectionResponse explore(Long userId, CareerExplorationRequest request);
    List<CareerDirectionResponse> getHistory(Long userId);
}
