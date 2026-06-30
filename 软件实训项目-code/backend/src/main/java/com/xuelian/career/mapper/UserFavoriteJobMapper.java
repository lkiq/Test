package com.xuelian.career.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuelian.career.entity.UserFavoriteJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏岗位 Mapper 接口
 */
@Mapper
public interface UserFavoriteJobMapper extends BaseMapper<UserFavoriteJob> {
}
