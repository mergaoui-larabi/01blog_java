package com.blog.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

        private final SecurityProperties properties;

        public SecurityConfig(SecurityProperties properties) {
                this.properties = properties;
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> {
                        properties.getPublicRoutes().forEach(route -> auth
                                        .requestMatchers(route.getMethod(), route.getPath())
                                        .permitAll());
                        auth.anyRequest().authenticated();
                }).build();
        }
}