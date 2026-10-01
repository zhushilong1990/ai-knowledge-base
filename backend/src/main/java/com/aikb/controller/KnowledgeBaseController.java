package com.aikb.controller;

import com.aikb.dto.CreateKBRequest;
import com.aikb.dto.KnowledgeBaseVO;
import com.aikb.entity.Document;
import com.aikb.entity.KnowledgeBase;
import com.aikb.service.KnowledgeBaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;

    public KnowledgeBaseController(KnowledgeBaseService kbService) {
        this.kbService = kbService;
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        Long userId = extractUserId(auth);
        return ResponseEntity.ok(kbService.listByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateKBRequest request, Authentication auth) {
        Long userId = extractUserId(auth);
        KnowledgeBase kb = kbService.create(request, userId);
        return ResponseEntity.ok(kb);
    }

    @GetMapping("/{kbId}/documents")
    public ResponseEntity<?> listDocuments(@PathVariable Long kbId, Authentication auth) {
        Long userId = extractUserId(auth);
        return ResponseEntity.ok(kbService.getDocuments(kbId, userId));
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
