package com.blog.moderation.config;

import com.blog.moderation.constant.SecurityConstants;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({ClientProperties.class, InternalSecurityProperties.class})
public class RestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(3);

    @Bean
    public RestClient userServiceRestClient(
            ClientProperties properties,
            InternalSecurityProperties internalProperties) {
        String url = properties.userService() != null && properties.userService().url() != null
                ? properties.userService().url()
                : "http://localhost:5001";

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
        requestFactory.setReadTimeout(READ_TIMEOUT);

        // User Service answers on /internal/users/**, which requires the shared internal token.
        return RestClient.builder()
                .baseUrl(url)
                .requestFactory(requestFactory)
                .defaultHeader(SecurityConstants.INTERNAL_TOKEN_HEADER, internalProperties.token())
                .build();
    }
}
