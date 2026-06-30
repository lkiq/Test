package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * 成长报告响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthReportResponse {
    private List<Map<String, Object>> timeline;
    private Map<String, Object> summary;
}
