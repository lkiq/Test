package com.xuelian.career.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuelian.career.entity.SkillTestQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 技能测试题库 Mapper 接口
 */
@Mapper
public interface SkillTestQuestionMapper extends BaseMapper<SkillTestQuestion> {

    /**
     * 按技能ID和阶段随机抽取指定数量的题目
     * @param skillId 技能ID
     * @param stage   学习阶段
     * @param limit   抽取数量
     * @return 题目列表
     */
    @Select("SELECT * FROM skill_test_question WHERE skill_id = #{skillId} AND stage = #{stage} AND is_deleted = 0 ORDER BY RAND() LIMIT #{limit}")
    List<SkillTestQuestion> selectRandomBySkillAndStage(@Param("skillId") Long skillId,
                                                         @Param("stage") String stage,
                                                         @Param("limit") int limit);

    /**
     * 统计某技能某阶段的题目总数
     */
    @Select("SELECT COUNT(*) FROM skill_test_question WHERE skill_id = #{skillId} AND stage = #{stage} AND is_deleted = 0")
    int countBySkillAndStage(@Param("skillId") Long skillId, @Param("stage") String stage);
}
