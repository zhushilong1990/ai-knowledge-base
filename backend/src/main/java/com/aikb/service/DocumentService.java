package com.aikb.service;

import com.aikb.dto.DocumentUploadResponse;
import com.aikb.entity.Document;
import com.aikb.mapper.DocumentMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx", "txt");

    @Value("${upload.temp-directory}")
    private String tempDirectory;

    @Value("${upload.max-size}")
    private long maxSize;

    @Value("${chroma.persist-directory}")
    private String chromaPath;

    @Value("${files.persist-directory}")
    private String filesPath;

    private final DocumentMapper documentMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentService(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    /**
     * Get all documents for a knowledge base.
     */
    public List<Document> getDocumentsByKbId(Long kbId) {
        return documentMapper.selectByKbId(kbId);
    }

    /**
     * Re-index all documents in a knowledge base.
     * Clears existing Chroma collection and rebuilds from stored files.
     */
    public Map<String, Object> reindexKnowledgeBase(Long kbId, Long userId) {
        List<Document> documents = documentMapper.selectByKbId(kbId);
        if (documents.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "completed");
            result.put("reindexed", 0);
            result.put("message", "No documents found for this knowledge base");
            return result;
        }

        // Build document list for reindex script
        List<Map<String, String>> docList = documents.stream()
                .filter(doc -> doc.getFilePath() != null && !doc.getFilePath().isEmpty())
                .map(doc -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("documentId", doc.getExternalDocumentId());
                    m.put("filePath", doc.getFilePath());
                    return m;
                })
                .collect(Collectors.toList());

        if (docList.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "completed");
            result.put("reindexed", 0);
            result.put("message", "No files available for reindexing");
            return result;
        }

        try {
            Map<String, Object> req = new HashMap<>();
            req.put("kbId", kbId);
            req.put("userId", userId);
            req.put("chromaPath", chromaPath);
            req.put("documents", docList);

            String result = executePythonScript(findReindexScriptPath(), objectMapper.writeValueAsString(req));
            JsonNode resultNode = objectMapper.readTree(result);

            if (resultNode.has("error")) {
                throw new RuntimeException("Reindex script error: " + resultNode.get("error").asText());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", resultNode.has("status") ? resultNode.get("status").asText() : "completed");
            response.put("reindexed", resultNode.has("reindexed") ? resultNode.get("reindexed").asInt() : docList.size());
            response.put("totalChunks", resultNode.has("totalChunks") ? resultNode.get("totalChunks").asInt() : 0);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Reindex failed: " + e.getMessage());
        }
    }

    /**
     * Delete a document and its vectors from Chroma.
     */
    public void deleteDocument(Long docId, Long userId) {
        Document doc = documentMapper.selectById(docId);
        if (doc == null) {
            throw new RuntimeException("Document not found");
        }
        if (!doc.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        Long kbId = doc.getKnowledgeBaseId();

        // 1. Delete from Chroma first
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("kbId", kbId);
            req.put("userId", userId);
            req.put("chromaPath", chromaPath);
            String result = executePythonScript(findDeleteScriptPath(), objectMapper.writeValueAsString(req));
            // result: { "deleted": count }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete vectors from Chroma: " + e.getMessage());
        }

        // 2. Delete from MySQL
        documentMapper.deleteById(docId);
    }

    public DocumentUploadResponse uploadDocument(MultipartFile file, Long kbId, Long userId) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type: " + extension
                    + ". Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + (maxSize / 1024 / 1024) + "MB");
        }

        Path uploadPath = Paths.get(tempDirectory).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        File tempFile = Files.createTempFile(uploadPath, "upload_", "." + extension).toFile();
        File persistentFile = null;
        try {
            file.transferTo(tempFile);

            String scriptPath = findScriptPath();
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("filePath", tempFile.getAbsolutePath());
            requestMap.put("kbId", kbId);
            requestMap.put("userId", userId);
            requestMap.put("chromaPath", chromaPath);
            String requestJson = objectMapper.writeValueAsString(requestMap);

            String result = executePythonScript(scriptPath, requestJson);
            JsonNode resultNode = objectMapper.readTree(result);

            if (resultNode.has("error")) {
                throw new RuntimeException("Python script error: " + resultNode.get("error").asText());
            }

            String externalDocumentId = resultNode.get("documentId").asText();
            int chunkCount = resultNode.get("chunkCount").asInt();
            String status = resultNode.has("status") ? resultNode.get("status").asText() : "completed";

            // Store file persistently for reindexing
            Path persistDir = Paths.get(filesPath, String.valueOf(userId), String.valueOf(kbId));
            Files.createDirectories(persistDir);
            persistentFile = persistDir.resolve(tempFile.getName()).toFile();
            Files.copy(tempFile.toPath(), persistentFile.toPath());

            // Persist document metadata to database
            Document document = Document.builder()
                    .fileName(originalFilename)
                    .fileSize(file.getSize())
                    .fileType(extension)
                    .knowledgeBaseId(kbId)
                    .userId(userId)
                    .externalDocumentId(externalDocumentId)
                    .filePath(persistentFile.getAbsolutePath())
                    .chunkCount(chunkCount)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            documentMapper.insert(document);

            DocumentUploadResponse response = new DocumentUploadResponse();
            response.setDocumentId(String.valueOf(document.getId()));
            response.setFileName(originalFilename);
            response.setChunkCount(chunkCount);
            response.setKnowledgeBaseId(kbId);
            response.setStatus(status);
            return response;

        } finally {
            if (tempFile.exists()) {
                Files.deleteIfExists(tempFile.toPath());
            }
        }
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
        Path scriptPath = workingDir.resolve("backend/scripts/extract_and_embed.py");
        if (Files.exists(scriptPath)) {
            return scriptPath.toString();
        }
        scriptPath = workingDir.resolve("scripts/extract_and_embed.py");
        if (Files.exists(scriptPath)) {
            return scriptPath.toString();
        }
        return "backend/scripts/extract_and_embed.py";
    }

    private String findDeleteScriptPath() {
        Path workingDir = Paths.get("").toAbsolutePath();
        Path scriptPath = workingDir.resolve("backend/scripts/delete_document.py");
        if (Files.exists(scriptPath)) {
            return scriptPath.toString();
        }
        scriptPath = workingDir.resolve("scripts/delete_document.py");
        if (Files.exists(scriptPath)) {
            return scriptPath.toString();
        }
        return "backend/scripts/delete_document.py";
    }

    private String findReindexScriptPath() {
        Path workingDir = Paths.get("").toAbsolutePath();
        Path scriptPath = workingDir.resolve("backend/scripts/reindex.py");
        if (Files.exists(scriptPath)) {
            return scriptPath.toString();
        }
        scriptPath = workingDir.resolve("scripts/reindex.py");
        if (Files.exists(scriptPath)) {
            return scriptPath.toString();
        }
        return "backend/scripts/reindex.py";
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }
}
