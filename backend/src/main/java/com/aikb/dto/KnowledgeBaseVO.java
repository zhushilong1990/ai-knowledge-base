package com.aikb.dto;

import lombok.Data;

/**
 * Knowledge base view object with document count.
 */
@Data
public class KnowledgeBaseVO {
    private Long id;
    private String name;
    private String description;
    private Integer docCount;
}
