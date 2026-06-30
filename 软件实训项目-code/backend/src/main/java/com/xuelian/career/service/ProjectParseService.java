package com.xuelian.career.service;

import com.xuelian.career.dto.response.EnterpriseRecommendResponse;

import java.util.List;

/**
 * 项目需求解析服务 - 调用 DeepSeek 分析项目描述，输出岗位建议和技能要求
 */
public interface ProjectParseService {

    /**
     * 解析项目描述，返回岗位建议
     * @param projectDescription 项目描述文本
     * @return 解析结果（projectSummary + positions），解析失败返回 null
     */
    ParseResult parseProject(String projectDescription);

    /**
     * 解析结果
     */
    class ParseResult {
        private String projectSummary;
        private List<EnterpriseRecommendResponse.PositionSuggestion> positions;

        public ParseResult() {}

        public ParseResult(String projectSummary, List<EnterpriseRecommendResponse.PositionSuggestion> positions) {
            this.projectSummary = projectSummary;
            this.positions = positions;
        }

        public String getProjectSummary() { return projectSummary; }
        public void setProjectSummary(String projectSummary) { this.projectSummary = projectSummary; }
        public List<EnterpriseRecommendResponse.PositionSuggestion> getPositions() { return positions; }
        public void setPositions(List<EnterpriseRecommendResponse.PositionSuggestion> positions) { this.positions = positions; }
    }
}
