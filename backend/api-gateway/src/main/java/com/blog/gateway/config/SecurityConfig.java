package com.blog.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(
                        ServerHttpSecurity http,
                        GatewaySecurityProperties securityProperties) {
                return http.csrf(csrf -> csrf.disable())
                                .authorizeExchange(exchange -> {
                                        securityProperties.getPublicRoutes()
                                                        .forEach(route -> exchange
                                                                        .pathMatchers(route)
                                                                        .permitAll());
                                        exchange.anyExchange().authenticated();
                                })
                                .build();
        }
}
