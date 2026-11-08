package com.blog.user.service;

import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserBlacklistService {
    private static final String BLACKLIST_KEY = "security:blocked-users";
    private final StringRedisTemplate redisTemplate;

    public UserBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void flagUser(UUID userId, String reason) {
        redisTemplate.opsForHash()
                .put(BLACKLIST_KEY, userId.toString(), reason);
    }

    public void unflagUser(UUID userId) {
        redisTemplate.opsForHash()
                .delete(BLACKLIST_KEY, userId.toString());
    }
}