package com.blog.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.blog.user.domain.valueobject.Gender;

@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record AdminBootstrapProperties(
                boolean enabled,
                String email,
                String username,
                String password,
                short age,
                Gender gender) {
}