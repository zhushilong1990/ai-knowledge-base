package com.aikb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Health check endpoint for deployment monitoring.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> result = new java.util.HashMap<>();
        result.put("status", "ok");
        result.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(result);
    }
}
