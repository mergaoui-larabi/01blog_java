package com.blog.post.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "post")
public record ClientProperties(
        ServiceConfig userService,
        ServiceConfig mediaService,
        ServiceConfig notificationService) {

    public record ServiceConfig(String url) {
    }
}
