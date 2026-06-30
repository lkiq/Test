package com.xuelian.career.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuelian.career.entity.AssessmentQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 测评题库 Mapper 接口
 */
@Mapper
public interface AssessmentQuestionMapper extends BaseMapper<AssessmentQuestion> {

    /**
     * 按维度随机抽取指定数量的题目
     * @param dimension 测评维度
     * @param limit     抽取数量
     * @return 题目列表
     */
    @Select("SELECT * FROM assessment_question WHERE dimension = #{dimension} AND is_deleted = 0 ORDER BY RAND() LIMIT #{limit}")
    List<AssessmentQuestion> selectRandomByDimension(@Param("dimension") String dimension, @Param("limit") int limit);
}
