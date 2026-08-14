package com.blog.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var adress = exchange.getRequest().getRemoteAddress();
            String ip = adress != null ? adress.getAddress().getHostAddress() : "unknown";
            return Mono.just(ip);
        };
    }
}
