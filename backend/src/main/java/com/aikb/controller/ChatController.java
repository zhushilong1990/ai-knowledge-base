package com.aikb.controller;

import com.aikb.dto.ChatRequest;
import com.aikb.dto.ChatResponse;
import com.aikb.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody ChatRequest request, Authentication auth) {
        Long userId = extractUserId(auth);
        try {
            ChatResponse response = chatService.askQuestion(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", "Failed to generate answer. Please try again later."));
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