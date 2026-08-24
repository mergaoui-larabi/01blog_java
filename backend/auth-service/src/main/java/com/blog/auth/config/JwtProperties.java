
package com.blog.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt")
public record JwtProperties(
        String issuer,
        long expiration,
        String keyId,
        String privateKeyPath) {
}