package com.blog.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Shared secret sent on User Service's /internal/** endpoints, which no longer accept anonymous calls.
 */
@ConfigurationProperties(prefix = "internal")
public record InternalSecurityProperties(String serviceToken) {

    public String token() {
        return serviceToken == null ? "" : serviceToken.trim();
    }
}
