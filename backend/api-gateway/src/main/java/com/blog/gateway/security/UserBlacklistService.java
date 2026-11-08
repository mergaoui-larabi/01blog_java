package com.blog.gateway.security;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserBlacklistService {

    private static final String BLACKLIST_KEY = "security:blocked-users";

    private final ReactiveStringRedisTemplate redisTemplate;

    public UserBlacklistService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> isFlagged(String userId) {
        return redisTemplate.opsForHash()
                .hasKey(BLACKLIST_KEY, userId);
    }

    public Mono<String> getFlagReason(String userId) {
        return redisTemplate.opsForHash()
                .get(BLACKLIST_KEY, userId)
                .cast(String.class);
    }
}