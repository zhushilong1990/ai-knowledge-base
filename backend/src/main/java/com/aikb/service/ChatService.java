package com.aikb.service;

import com.aikb.dto.ChatRequest;
import com.aikb.dto.ChatResponse;
import com.aikb.entity.ChatMessage;
import com.aikb.entity.ChatSession;
import com.aikb.entity.KnowledgeBase;
import com.aikb.mapper.ChatMessageMapper;
import com.aikb.mapper.ChatSessionMapper;
import com.aikb.mapper.KnowledgeBaseMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Value("${chroma.persist-directory}")
    private String chromaPath;

    @Value("${siliconflow.api-key}")
    private String siliconflowApiKey;

    @Value("${embedding.provider}")
    private String embedProvider;

    @Value("${embedding.api-key}")
    private String embedApiKey;

    @Value("${embedding.base-url}")
    private String embedBaseUrl;

    @Value("${embedding.model}")
    private String embedModel;

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ChatSessionMapper sessionMapper, ChatMessageMapper messageMapper,
                      KnowledgeBaseMapper knowledgeBaseMapper) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
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
        scriptRequest.put("chromaPath", chromaPath);

        // Query the knowledge base to find its actual owner userId (for ChromaDB collection naming)
        Long ownerUserId = userId;
        try {
            KnowledgeBase kb = knowledgeBaseMapper.selectById(request.getKbId());
            if (kb != null) {
                ownerUserId = kb.getUserId();
            }
        } catch (Exception e) {
            log.warn("Failed to query KB owner, using request userId: {}", e.getMessage());
        }
        scriptRequest.put("userId", ownerUserId);

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

    /**
     * Execute Python script using file-based communication to avoid stdin/stdout pipe encoding issues on Windows.
     * Writes request JSON to a temp file, Python reads it and writes output to another temp file.
     */
    private String executePythonScript(String scriptPath, String requestJson) throws Exception {
        log.info("Executing Python script: {} with request: {}", scriptPath, requestJson);

        // Write request JSON to temp file (bypass stdin pipe encoding issues on Windows)
        Path inputFile = Files.createTempFile("chat_input_", ".json");
        Path outputFile = Files.createTempFile("chat_output_", ".json");
        inputFile.toFile().deleteOnExit();
        outputFile.toFile().deleteOnExit();

        Files.write(inputFile, requestJson.getBytes("UTF-8"));

        ProcessBuilder pb = new ProcessBuilder("python", scriptPath, inputFile.toString(), outputFile.toString());
        pb.environment().put("SILICON_FLOW_API_KEY", siliconflowApiKey);
        pb.environment().put("EMBED_PROVIDER", embedProvider);
        pb.environment().put("EMBED_API_KEY", embedApiKey);
        pb.environment().put("EMBED_BASE_URL", embedBaseUrl);
        pb.environment().put("EMBED_MODEL", embedModel);

        Process process = pb.start();

        boolean finished = process.waitFor(120, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Python script timed out after 120 seconds");
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException("Python script failed with exit code " + process.exitValue());
        }

        // Read output from file (bypass stdout pipe encoding issues on Windows)
        String result = new String(Files.readAllBytes(outputFile), "UTF-8");
        log.info("Python script finished, exitCode: 0, output length: {}", result.length());

        return result.trim();
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

    /**
     * Delete a chat session and all its messages.
     * @throws RuntimeException if session not found or not owned by user
     */
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        // Delete messages first (foreign key constraint)
        chatMessageMapper.deleteBySessionId(sessionId);
        // Delete session
        chatSessionMapper.deleteById(sessionId);
    }
}
