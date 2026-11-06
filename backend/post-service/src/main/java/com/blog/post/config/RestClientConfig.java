package com.blog.post.config;

import com.blog.post.constant.SecurityConstants;

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
    public RestClient userServiceRestClient(ClientProperties properties, InternalSecurityProperties internalProperties) {
        String url = properties.userService() != null && properties.userService().url() != null
                ? properties.userService().url()
                : "http://localhost:5001";

        return RestClient.builder()
                .baseUrl(url)
                .defaultHeader(SecurityConstants.INTERNAL_TOKEN_HEADER, internalProperties.token())
                .build();
    }

    @Bean
    public RestClient mediaServiceRestClient(ClientProperties properties) {
        String url = properties.mediaService() != null && properties.mediaService().url() != null
                ? properties.mediaService().url()
                : "http://localhost:5003";

        return RestClient.builder()
                .baseUrl(url)
                .build();
    }

    @Bean
    public RestClient notificationServiceRestClient(
            ClientProperties properties,
            InternalSecurityProperties internalProperties) {
        String url = properties.notificationService() != null && properties.notificationService().url() != null
                ? properties.notificationService().url()
                : "http://localhost:5005";

        return RestClient.builder()
                .baseUrl(url)
                .requestFactory(timeoutRequestFactory())
                .defaultHeader(SecurityConstants.INTERNAL_TOKEN_HEADER, internalProperties.token())
                .build();
    }

    private JdkClientHttpRequestFactory timeoutRequestFactory() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return requestFactory;
    }
}
