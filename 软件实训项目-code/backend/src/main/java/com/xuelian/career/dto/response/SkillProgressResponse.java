package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 技能掌握度响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillProgressResponse {
    private Map<String, Double> skillScores;
    private Map<String, String> skillLevels;
}
