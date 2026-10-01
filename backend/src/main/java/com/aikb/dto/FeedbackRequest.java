package com.aikb.dto;

import lombok.Data;

/**
 * Feedback request DTO.
 */
@Data
public class FeedbackRequest {
    private Long messageId;
    private String rating;
    private String feedbackReason;
}