package com.aikb.mapper;

import com.aikb.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT cm.*, cf.rating as feedback " +
            "FROM chat_message cm " +
            "LEFT JOIN chat_feedback cf ON cm.id = cf.message_id AND cf.user_id = #{userId} " +
            "WHERE cm.session_id = #{sessionId} " +
            "ORDER BY cm.created_at ASC")
    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    @Delete("DELETE FROM chat_message WHERE session_id = #{sessionId}")
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}