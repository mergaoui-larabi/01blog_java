package com.blog.post.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Shared secret used for service-to-service calls on /internal/** endpoints. Fail closed: no token
 * configured means no internal call is ever accepted.
 */
@ConfigurationProperties(prefix = "internal")
public record InternalSecurityProperties(String serviceToken) {

    public String token() {
        return serviceToken == null ? "" : serviceToken.trim();
    }
}
