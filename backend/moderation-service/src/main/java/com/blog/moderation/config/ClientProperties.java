package com.blog.moderation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "moderation")
public record ClientProperties(
        ServiceConfig userService) {

    public record ServiceConfig(String url) {
    }
}
