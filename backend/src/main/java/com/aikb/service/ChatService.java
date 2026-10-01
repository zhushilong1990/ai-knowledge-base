package com.aikb.service;

import com.aikb.dto.ChatRequest;
import com.aikb.dto.ChatResponse;
import com.aikb.entity.ChatMessage;
import com.aikb.entity.ChatSession;
import com.aikb.mapper.ChatMessageMapper;
import com.aikb.mapper.ChatSessionMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class ChatService {

    @Value("${chroma.persist-directory}")
    private String chromaPath;

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    public ChatResponse askQuestion(ChatRequest request, Long userId) {
        Long sessionId = request.getSessionId();

        if (sessionId == null) {
            ChatSession session = ChatSession.builder()
                    .userId(userId)
                    .title(request.getQuestion().substring(0, Math.min(30, request.getQuestion().length())))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            sessionMapper.insert(session);
            sessionId = session.getId();
        }

        Map<String, Object> scriptRequest = new HashMap<>();
        scriptRequest.put("question", request.getQuestion());
        scriptRequest.put("kbId", request.getKbId());
        scriptRequest.put("userId", userId);
        scriptRequest.put("chromaPath", chromaPath);

        String result;
        try {
            result = executePythonScript(findScriptPath(), objectMapper.writeValueAsString(scriptRequest));
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute chat script: " + e.getMessage(), e);
        }

        ChatResponse chatResponse;
        try {
            Map<String, Object> resultMap = objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {});
            if (resultMap.containsKey("error")) {
                throw new RuntimeException("Chat script error: " + resultMap.get("error"));
            }

            chatResponse = new ChatResponse();
            chatResponse.setSessionId(sessionId);
            chatResponse.setAnswer((String) resultMap.get("answer"));

            List<Map<String, Object>> sourcesList = (List<Map<String, Object>>) resultMap.get("sources");
            if (sourcesList != null) {
                List<ChatResponse.Source> sources = sourcesList.stream()
                        .map(s -> {
                            ChatResponse.Source source = new ChatResponse.Source();
                            source.setId((String) s.get("id"));
                            source.setText((String) s.get("text"));
                            source.setScore(((Number) s.get("score")).doubleValue());
                            return source;
                        })
                        .collect(Collectors.toList());
                chatResponse.setSources(sources);
            } else {
                chatResponse.setSources(Collections.emptyList());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse chat script response: " + e.getMessage(), e);
        }

        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(request.getQuestion())
                .createdAt(LocalDateTime.now())
                .build();
        messageMapper.insert(userMessage);

        String sourcesJson;
        try {
            sourcesJson = objectMapper.writeValueAsString(chatResponse.getSources());
        } catch (JsonProcessingException e) {
            sourcesJson = "[]";
        }

        ChatMessage assistantMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role("assistant")
                .content(chatResponse.getAnswer())
                .sources(sourcesJson)
                .createdAt(LocalDateTime.now())
                .build();
        messageMapper.insert(assistantMessage);

        return chatResponse;
    }

    private String executePythonScript(String scriptPath, String requestJson) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
        pb.redirectErrorStream(false);

        Process process = pb.start();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream(), "UTF-8"))) {
            writer.write(requestJson);
            writer.flush();
        }

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        Thread stdoutReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                }
            } catch (Exception e) {
                // ignore
            }
        });

        Thread stderrReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append("\n");
                }
            } catch (Exception e) {
                // ignore
            }
        });

        stdoutReader.start();
        stderrReader.start();

        boolean finished = process.waitFor(120, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Python script timed out after 120 seconds");
        }

        stdoutReader.join(1000);
        stderrReader.join(1000);

        if (process.exitValue() != 0) {
            throw new RuntimeException("Python script failed with exit code " + process.exitValue()
                    + ". stderr: " + stderr.toString());
        }

        return stdout.toString().trim();
    }

    private String findScriptPath() {
        Path workingDir = Paths.get("").toAbsolutePath();
        Path scriptPath = workingDir.resolve("backend/scripts/chat_and_answer.py");
        if (Files.exists(scriptPath)) {
            return scriptPath.toString();
        }
        scriptPath = workingDir.resolve("scripts/chat_and_answer.py");
        if (Files.exists(scriptPath)) {
            return scriptPath.toString();
        }
        return "backend/scripts/chat_and_answer.py";
    }

    /**
     * Get all chat sessions for a user, ordered by updated_at desc.
     */
    public List<ChatSession> getSessionsByUserId(Long userId) {
        return sessionMapper.selectList(
            new QueryWrapper<ChatSession>()
                .eq("user_id", userId)
                .orderByDesc("updated_at")
        );
    }

    /**
     * Get chat history for a session. Validates session belongs to user.
     * @throws RuntimeException if session not found or not owned by user
     */
    public List<ChatMessage> getChatHistory(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        return messageMapper.selectBySessionId(sessionId, userId);
    }
}