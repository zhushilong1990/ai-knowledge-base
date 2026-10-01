package com.aikb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Chat message entity for storing individual messages in a chat session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("role")
    private String role;

    @TableField("content")
    private String content;

    @TableField("sources")
    private String sources;

    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * Transient field for feedback status - not stored in DB, loaded via JOIN.
     * Values: 'like', 'dislike', or null
     */
    @TableField(exist = false)
    private String feedback;
}