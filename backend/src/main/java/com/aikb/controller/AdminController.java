package com.aikb.controller;

import com.aikb.entity.Document;
import com.aikb.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin endpoints for maintenance operations.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DocumentService documentService;

    public AdminController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Re-index all documents in a knowledge base.
     * Clears existing Chroma collection and rebuilds vectors from stored files.
     */
    @PostMapping("/reindex/{knowledgeBaseId}")
    public ResponseEntity<?> reindex(@PathVariable Long knowledgeBaseId, Authentication auth) {
        Long userId = extractUserId(auth);
        try {
            Map<String, Object> result = documentService.reindexKnowledgeBase(knowledgeBaseId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get Chroma collection stats for a knowledge base.
     */
    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam Long kbId, Authentication auth) {
        Long userId = extractUserId(auth);
        try {
            List<Document> documents = documentService.getDocumentsByKbId(kbId);
            int docCount = documents.size();
            int totalChunks = 0;
            for (Document doc : documents) {
                if (doc.getChunkCount() != null) {
                    totalChunks += doc.getChunkCount();
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("kbId", kbId);
            result.put("documentCount", docCount);
            result.put("totalChunks", totalChunks);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 1L;
        }
        try {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                return (long) email.hashCode() & 0xFFFFFFFFL;
            }
            return Long.parseLong(auth.getName());
        } catch (Exception e) {
            return 1L;
        }
    }
}
