package com.blog.user.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.blog.user.constant.SecurityConstants;

@Configuration
@EnableConfigurationProperties({ClientProperties.class, InternalSecurityProperties.class})
public class RestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    public RestClient postServiceRestClient(
            ClientProperties properties,
            InternalSecurityProperties internalProperties) {
        return cleanupClient(url(properties.postService(), "http://localhost:5004"), internalProperties);
    }

    @Bean
    public RestClient notificationServiceRestClient(
            ClientProperties properties,
            InternalSecurityProperties internalProperties) {
        return cleanupClient(url(properties.notificationService(), "http://localhost:5005"), internalProperties);
    }

    @Bean
    public RestClient moderationServiceRestClient(
            ClientProperties properties,
            InternalSecurityProperties internalProperties) {
        return cleanupClient(url(properties.moderationService(), "http://localhost:5006"), internalProperties);
    }

    private RestClient cleanupClient(String baseUrl, InternalSecurityProperties internalProperties) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
        requestFactory.setReadTimeout(READ_TIMEOUT);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(SecurityConstants.INTERNAL_TOKEN_HEADER, internalProperties.token())
                .build();
    }

    private String url(ClientProperties.ServiceConfig config, String fallback) {
        return config != null && config.url() != null ? config.url() : fallback;
    }
}
