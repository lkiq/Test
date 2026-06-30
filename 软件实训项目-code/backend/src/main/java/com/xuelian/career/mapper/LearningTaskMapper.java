package com.xuelian.career.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuelian.career.entity.LearningTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 学习任务 Mapper 接口
 */
@Mapper
public interface LearningTaskMapper extends BaseMapper<LearningTask> {

    /** 查询用户对某技能的最新学习任务记录 */
    @Select("SELECT * FROM learning_task WHERE user_id = #{userId} AND skill_id = #{skillId} ORDER BY created_at DESC LIMIT 1")
    LearningTask findLatestByUserAndSkill(@Param("userId") Long userId, @Param("skillId") Long skillId);
}
