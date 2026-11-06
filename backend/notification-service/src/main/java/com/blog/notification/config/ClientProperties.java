package com.blog.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public record ClientProperties(
        ServiceConfig userService) {

    public record ServiceConfig(String url) {
    }
}
