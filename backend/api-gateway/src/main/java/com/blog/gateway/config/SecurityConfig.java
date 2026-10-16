package com.blog.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(
                        ServerHttpSecurity http,
                        GatewaySecurityProperties securityProperties) {
                return http
                                .cors(cors -> {
                                })
                                .csrf(csrf -> csrf.disable())
                                .authorizeExchange(exchange -> {
                                        exchange.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                                        securityProperties.getPublicRoutes()
                                                        .forEach(route -> exchange
                                                                        .pathMatchers(route)
                                                                        .permitAll());
                                        exchange.anyExchange().authenticated();
                                })
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                                }))
                                .build();
        }

        @Bean
        public ReactiveJwtDecoder jwtDecoder(JwtProperties jwtProperties) {

                NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwtProperties.jwkSetUri())
                                .build();
                decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));

                return decoder;
        }
}
