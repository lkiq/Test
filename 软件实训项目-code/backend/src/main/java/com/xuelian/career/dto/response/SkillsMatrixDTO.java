package com.xuelian.career.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 技能-岗位映射 DTO - 展示每个技能来自哪些岗位
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillsMatrixDTO {
    private Long skillId;
    private String skillName;
    private List<String> sourceJobs;  // ["Java后端开发", "测试开发"]
    private String category;
}
