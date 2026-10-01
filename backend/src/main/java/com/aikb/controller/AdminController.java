package com.aikb.controller;

import com.aikb.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get Chroma collection stats for a knowledge base.
     */
    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam Long kbId, Authentication auth) {
        Long userId = extractUserId(auth);
        try {
            var documents = documentService.getDocumentsByKbId(kbId);
            int docCount = documents.size();
            int totalChunks = documents.stream()
                    .mapToInt(doc -> doc.getChunkCount() != null ? doc.getChunkCount() : 0)
                    .sum();
            return ResponseEntity.ok(Map.of(
                    "kbId", kbId,
                    "documentCount", docCount,
                    "totalChunks", totalChunks
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
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
                return email.hashCode() & 0xFFFFFFFFL;
            }
            return Long.parseLong(auth.getName());
        } catch (Exception e) {
            return 1L;
        }
    }
}
