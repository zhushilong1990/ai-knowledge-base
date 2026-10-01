package com.aikb.controller;

import com.aikb.dto.DocumentUploadResponse;
import com.aikb.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("knowledgeBaseId") Long kbId,
            Authentication auth) {

        Long userId = extractUserId(auth);

        try {
            DocumentUploadResponse response = documentService.uploadDocument(file, kbId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Upload failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{docId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long docId, Authentication auth) {
        Long userId = extractUserId(auth);
        try {
            documentService.deleteDocument(docId, userId);
            return ResponseEntity.ok(Collections.singletonMap("message", "Document deleted"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(403)
                        .body(Collections.singletonMap("error", e.getMessage()));
            }
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", "Failed to delete document"));
        }
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 1L;
        }
        try {
            Object principal = auth.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                String email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                return email.hashCode() & 0xFFFFFFFFL;
            }
            return Long.parseLong(auth.getName());
        } catch (Exception e) {
            return 1L;
        }
    }
}
