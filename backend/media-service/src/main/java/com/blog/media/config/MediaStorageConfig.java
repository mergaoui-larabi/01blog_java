package com.blog.media.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class MediaStorageConfig {

    @Bean
    Path mediaStoragePath(
            @Value("${media.storage.location}") String location) throws IOException {
        Path path = Path.of(location)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(path);

        return path;
    }
}