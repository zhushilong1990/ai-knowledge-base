package com.aikb.dto;

import lombok.Data;

/**
 * Chat request DTO.
 */
@Data
public class ChatRequest {
    private String question;
    private Long kbId;
    private Long sessionId;
}