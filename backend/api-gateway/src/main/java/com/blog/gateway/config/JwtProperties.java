package com.blog.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.jwt")
public record JwtProperties(
        String jwkSetUri,
        String issuer
) {
}