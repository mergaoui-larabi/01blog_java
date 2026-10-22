package com.blog.post.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient userServiceRestClient(ClientProperties properties) {
        String url = properties.userService() != null && properties.userService().url() != null
                ? properties.userService().url()
                : "http://localhost:5001";

        return RestClient.builder()
                .baseUrl(url)
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
}
