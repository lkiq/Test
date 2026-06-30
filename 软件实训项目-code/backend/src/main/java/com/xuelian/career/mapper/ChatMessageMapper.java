package com.xuelian.career.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuelian.career.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息 Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
