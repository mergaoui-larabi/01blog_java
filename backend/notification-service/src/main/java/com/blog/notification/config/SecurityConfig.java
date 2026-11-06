package com.blog.notification.config;

import com.blog.notification.constant.SecurityConstants;
import com.blog.notification.filter.InternalTokenFilter;

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
                        .requestMatchers(
                                "/actuator/health",
                                // The STOMP handshake is authenticated by StompAuthHandshakeInterceptor
                                "/ws/notifications")
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
