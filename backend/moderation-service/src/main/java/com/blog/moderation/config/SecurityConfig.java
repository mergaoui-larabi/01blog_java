package com.blog.moderation.config;

import com.blog.moderation.constant.SecurityConstants;
import com.blog.moderation.filter.InternalTokenFilter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(InternalSecurityProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, InternalSecurityProperties internalProperties)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health")
                        .permitAll()
                        .requestMatchers("/internal/**")
                        .hasAuthority(SecurityConstants.INTERNAL_SERVICE_AUTHORITY)
                        .anyRequest().authenticated())
                .addFilterBefore(
                        new InternalTokenFilter(internalProperties.token()),
                        BearerTokenAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                }))
                .build();
    }
}
