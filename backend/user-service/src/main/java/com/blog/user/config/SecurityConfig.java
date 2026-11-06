package com.blog.user.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import com.blog.user.constant.SecurityConstants;
import com.blog.user.filter.InternalTokenFilter;

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
                                "/api/v1/users/signup")
                        .permitAll()
                        .requestMatchers("/internal/users/**")
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
