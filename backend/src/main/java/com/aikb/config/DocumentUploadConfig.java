package com.aikb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class DocumentUploadConfig {

    @Value("${upload.temp-directory}")
    private String tempDirectory;

    @Value("${upload.max-size}")
    private long maxSize;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(maxSize));
        factory.setMaxRequestSize(DataSize.ofBytes(maxSize));
        return factory.createMultipartConfig();
    }

    @Bean
    public Path uploadDirectory() throws Exception {
        Path path = Paths.get(tempDirectory).toAbsolutePath();
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }
}
