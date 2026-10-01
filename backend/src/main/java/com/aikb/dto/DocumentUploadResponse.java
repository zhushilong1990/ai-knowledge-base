package com.aikb.dto;

public class DocumentUploadResponse {

    private String documentId;
    private String fileName;
    private int chunkCount;
    private Long knowledgeBaseId;
    private String status;
    private String message;

    public DocumentUploadResponse() {}

    public DocumentUploadResponse(String documentId, String fileName, int chunkCount,
            Long knowledgeBaseId, String status, String message) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.chunkCount = chunkCount;
        this.knowledgeBaseId = knowledgeBaseId;
        this.status = status;
        this.message = message;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public Long getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(Long knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
