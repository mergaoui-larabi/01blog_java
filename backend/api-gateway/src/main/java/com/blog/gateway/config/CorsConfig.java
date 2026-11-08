package com.blog.gateway.config;

import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Exposes the gateway's global CORS configuration as a {@link CorsConfigurationSource}
 * bean.
 *
 * <p>
 * Spring Security's {@code http.cors(...)} only installs a {@code CorsWebFilter} when a
 * {@code CorsConfigurationSource} bean exists. Without this bean, CORS was handled only
 * by the gateway's route handler mapping, which runs after the security filter chain.
 * Responses produced by security itself (401/403, including the banned/deleted account
 * rejections) therefore had no {@code Access-Control-Allow-Origin} header.
 *
 * <p>
 * The configuration is taken from the same
 * {@code spring.cloud.gateway.server.webflux.globalcors} properties used by the gateway,
 * so there is a single source of truth.
 */
@Configuration(proxyBeanMethods = false)
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(GlobalCorsProperties globalCorsProperties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        globalCorsProperties.getCorsConfigurations()
                .forEach(source::registerCorsConfiguration);
        return source;
    }
}
