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
 * Document entity for storing document metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("documents")
public class Document {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Long fileSize;

    @TableField("file_type")
    private String fileType;

    @TableField("knowledge_base_id")
    private Long knowledgeBaseId;

    @TableField("user_id")
    private Long userId;

    @TableField("external_document_id")
    private String externalDocumentId;

    @TableField("file_path")
    private String filePath;

    @TableField("chunk_count")
    private Integer chunkCount;

    @TableField("status")
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
