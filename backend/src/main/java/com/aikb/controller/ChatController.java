package com.aikb.controller;

import com.aikb.dto.ChatRequest;
import com.aikb.dto.ChatResponse;
import com.aikb.dto.FeedbackRequest;
import com.aikb.entity.ChatFeedback;
import com.aikb.entity.ChatMessage;
import com.aikb.entity.ChatSession;
import com.aikb.security.JwtTokenProvider;
import com.aikb.service.ChatFeedbackService;
import com.aikb.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatFeedbackService chatFeedbackService;
    private final JwtTokenProvider jwtTokenProvider;

    public ChatController(ChatService chatService, ChatFeedbackService chatFeedbackService, JwtTokenProvider jwtTokenProvider) {
        this.chatService = chatService;
        this.chatFeedbackService = chatFeedbackService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody ChatRequest request, Authentication auth, HttpServletRequest httpRequest) {
        Long userId = extractUserId(auth, httpRequest);
        try {
            ChatResponse response = chatService.askQuestion(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", "Failed to generate answer. Please try again later."));
        }
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(Authentication auth, HttpServletRequest httpRequest) {
        Long userId = extractUserId(auth, httpRequest);
        List<ChatSession> sessions = chatService.getSessionsByUserId(userId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<?> getHistory(@PathVariable Long sessionId, Authentication auth, HttpServletRequest httpRequest) {
        Long userId = extractUserId(auth, httpRequest);
        try {
            List<ChatMessage> messages = chatService.getChatHistory(sessionId, userId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(403)
                        .body(Collections.singletonMap("error", e.getMessage()));
            }
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", "Failed to load chat history"));
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest request, Authentication auth, HttpServletRequest httpRequest) {
        Long userId = extractUserId(auth, httpRequest);
        if (request.getRating() == null ||
            (!request.getRating().equals("like") && !request.getRating().equals("dislike"))) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Rating must be 'like' or 'dislike'"));
        }
        try {
            ChatFeedback feedback = chatFeedbackService.submitFeedback(
                    request.getMessageId(),
                    userId,
                    request.getRating(),
                    request.getFeedbackReason()
            );
            return ResponseEntity.ok(feedback);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404)
                        .body(Collections.singletonMap("error", e.getMessage()));
            }
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", "Failed to submit feedback"));
        }
    }

    @GetMapping("/feedback/{messageId}")
    public ResponseEntity<?> getFeedback(@PathVariable Long messageId, Authentication auth, HttpServletRequest httpRequest) {
        Long userId = extractUserId(auth, httpRequest);
        ChatFeedback feedback = chatFeedbackService.getFeedback(messageId, userId);
        if (feedback == null) {
            return ResponseEntity.status(404)
                    .body(Collections.singletonMap("error", "Feedback not found"));
        }
        return ResponseEntity.ok(feedback);
    }

    private Long extractUserId(Authentication auth, HttpServletRequest httpRequest) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 1L;
        }
        try {
            // Extract token from Authorization header and get real userId from JWT claim
            String bearerToken = httpRequest.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                if (userId != null) {
                    return userId;
                }
            }
            // Fallback: use email hashCode (old behavior, kept for safety)
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