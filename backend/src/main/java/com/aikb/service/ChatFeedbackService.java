package com.aikb.service;

import com.aikb.entity.ChatFeedback;
import com.aikb.entity.ChatMessage;
import com.aikb.mapper.ChatFeedbackMapper;
import com.aikb.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for managing chat message feedback (like/dislike).
 */
@Service
public class ChatFeedbackService {

    private final ChatFeedbackMapper feedbackMapper;
    private final ChatMessageMapper messageMapper;

    public ChatFeedbackService(ChatFeedbackMapper feedbackMapper, ChatMessageMapper messageMapper) {
        this.feedbackMapper = feedbackMapper;
        this.messageMapper = messageMapper;
    }

    /**
     * Submit or update feedback for a message.
     * If feedback already exists for this user and message, update it.
     * Otherwise, insert new feedback.
     *
     * @param messageId the message ID
     * @param userId the user ID
     * @param rating 'like' or 'dislike'
     * @param reason optional feedback reason
     * @return the saved ChatFeedback
     * @throws RuntimeException if message not found
     */
    public ChatFeedback submitFeedback(Long messageId, Long userId, String rating, String reason) {
        ChatMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new RuntimeException("Message not found");
        }

        ChatFeedback existing = feedbackMapper.selectByMessageIdAndUserId(messageId, userId);
        if (existing != null) {
            existing.setRating(rating);
            existing.setFeedbackReason(reason);
            existing.setUpdatedAt(LocalDateTime.now());
            feedbackMapper.updateById(existing);
            return existing;
        }

        ChatFeedback feedback = ChatFeedback.builder()
                .messageId(messageId)
                .userId(userId)
                .rating(rating)
                .feedbackReason(reason)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        feedbackMapper.insert(feedback);
        return feedback;
    }

    /**
     * Get feedback for a specific message by the current user.
     *
     * @param messageId the message ID
     * @param userId the user ID
     * @return the ChatFeedback or null if not found
     */
    public ChatFeedback getFeedback(Long messageId, Long userId) {
        return feedbackMapper.selectByMessageIdAndUserId(messageId, userId);
    }
}