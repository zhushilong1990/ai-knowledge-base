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

    @Value("${llm.api-key}")
    private String llmApiKey;

    @Value("${llm.base-url}")
    private String llmBaseUrl;

    @Value("${llm.model}")
    private String llmModel;

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
                    .kbId(request.getKbId())
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
                            Object scoreObj = s.get("score");
                            if (scoreObj != null) {
                                source.setScore(((Number) scoreObj).doubleValue());
                            }
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
        log.info("Executing Python script: {} with request: {}", scriptPath, requestJson);

        String pythonCommand = "python3";
        String pythonHome = System.getenv("PYTHON_HOME");
        if (pythonHome != null && !pythonHome.isEmpty()) {
            pythonCommand = pythonHome + "/python";
        }

        Path inputFile = Files.createTempFile("chat_input_", ".json");
        Path outputFile = Files.createTempFile("chat_output_", ".json");
        inputFile.toFile().deleteOnExit();
        outputFile.toFile().deleteOnExit();

        Files.write(inputFile, requestJson.getBytes("UTF-8"));

        ProcessBuilder pb = new ProcessBuilder(pythonCommand, scriptPath, inputFile.toString(), outputFile.toString());
        pb.environment().put("LLM_API_KEY", llmApiKey);
        pb.environment().put("LLM_BASE_URL", llmBaseUrl);
        pb.environment().put("LLM_MODEL", llmModel);
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
            StringBuilder sb = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            String stderr = sb.toString();
            log.error("Python script failed: {}", stderr);
            throw new RuntimeException("Python script failed with exit code " + process.exitValue() + ": " + stderr);
        }

        byte[] outputBytes = java.nio.file.Files.readAllBytes(outputFile);
        String result = new String(outputBytes, "UTF-8");
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

    public List<ChatSession> getSessionsByUserId(Long userId) {
        return sessionMapper.selectList(
            new QueryWrapper<ChatSession>()
                .eq("user_id", userId)
                .orderByDesc("updated_at")
        );
    }

    public List<ChatMessage> getChatHistory(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        List<ChatMessage> messages = messageMapper.selectBySessionId(sessionId, userId);
        // Populate kbId from session for each message
        for (ChatMessage msg : messages) {
            msg.setKbId(session.getKbId());
        }
        return messages;
    }

    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        messageMapper.deleteBySessionId(sessionId);
        sessionMapper.deleteById(sessionId);
    }
}
