package com.xuelian.career.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 雷达图数据 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadarChartVO {
    private List<String> dimensions;
    private List<Double> userValues;
    private List<Double> requiredValues;
}
