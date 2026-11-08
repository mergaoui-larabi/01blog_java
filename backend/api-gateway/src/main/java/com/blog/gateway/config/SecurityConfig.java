package com.blog.gateway.config;

import com.blog.gateway.security.AccountRestrictedException;
import com.blog.gateway.security.CustomAccessDeniedHandler;
import com.blog.gateway.security.UserBlacklistService;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(
                        ServerHttpSecurity http,
                        GatewaySecurityProperties securityProperties,
                        UserBlacklistService blacklistService,
                        CustomAccessDeniedHandler accessDeniedHandler) {

                return http.cors(cors -> {
                }).csrf(csrf -> csrf
                                .disable())
                                .exceptionHandling(exception -> exception.accessDeniedHandler(accessDeniedHandler))
                                .authorizeExchange(exchange -> {
                                        exchange.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                                        securityProperties.getPublicRoutes()
                                                        .forEach(route -> exchange
                                                                        .pathMatchers(route)
                                                                        .permitAll());
                                        exchange.anyExchange().access((authentication, context) -> authentication
                                                        .filter(Authentication::isAuthenticated)
                                                        .cast(JwtAuthenticationToken.class)
                                                        .flatMap(auth -> {
                                                                String userId = auth.getToken().getSubject();

                                                                return blacklistService.getFlagReason(userId)
                                                                                .flatMap(reason -> {
                                                                                        String code = switch (reason) {
                                                                                                case "BANNED" ->
                                                                                                        "ACCOUNT_BANNED";
                                                                                                case "DELETED" ->
                                                                                                        "ACCOUNT_DELETED";
                                                                                                default -> null;
                                                                                        };
                                                                                        if (code == null) {
                                                                                                return Mono.just(
                                                                                                                new AuthorizationDecision(
                                                                                                                                true));
                                                                                        }
                                                                                        return Mono.error(
                                                                                                        new AccountRestrictedException(
                                                                                                                        code));
                                                                                })
                                                                                .defaultIfEmpty(
                                                                                                new AuthorizationDecision(
                                                                                                                true));
                                                        }));
                                })
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                                }))
                                .build();
        }

        @Bean
        public ReactiveJwtDecoder jwtDecoder(JwtProperties jwtProperties) {
                NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                                .withJwkSetUri(jwtProperties.jwkSetUri())
                                .build();
                decoder.setJwtValidator(
                                JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));
                return decoder;
        }
}