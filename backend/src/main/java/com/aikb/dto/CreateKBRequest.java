package com.aikb.dto;

import lombok.Data;

/**
 * Request object for creating a knowledge base.
 */
@Data
public class CreateKBRequest {
    private String name;
    private String description;
}
