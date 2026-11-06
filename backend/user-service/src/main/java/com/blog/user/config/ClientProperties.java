package com.blog.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user-service")
public record ClientProperties(
        ServiceConfig postService,
        ServiceConfig notificationService,
        ServiceConfig moderationService) {

    public record ServiceConfig(String url) {
    }
}
