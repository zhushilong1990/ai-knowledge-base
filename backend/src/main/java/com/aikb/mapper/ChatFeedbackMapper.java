package com.aikb.mapper;

import com.aikb.entity.ChatFeedback;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatFeedbackMapper extends BaseMapper<ChatFeedback> {

    @Select("SELECT * FROM chat_feedback WHERE message_id = #{messageId} AND user_id = #{userId}")
    ChatFeedback selectByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
}