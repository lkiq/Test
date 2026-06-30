package com.xuelian.career.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 技能缺口 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapVO {
    private String skillName;
    private String userLevel;
    private String requiredLevel;
    private String gapDegree;
    /** 该技能所属来源岗位（多岗位合并模式） */
    private List<String> sourceJobs;
}
