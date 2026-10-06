package com.aikb.dto;

import lombok.Data;

import java.util.List;

/**
 * Chat response DTO.
 */
@Data
public class ChatResponse {
    private String answer;
    private List<Source> sources;
    private Long sessionId;
    private Long messageId;  // assistant message ID for feedback

    @Data
    public static class Source {
        private String id;
        private String text;
        private Double score;
    }
}