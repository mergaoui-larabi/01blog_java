package com.blog.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.user-service")
public record UserServiceProperties(
        String url) {
}