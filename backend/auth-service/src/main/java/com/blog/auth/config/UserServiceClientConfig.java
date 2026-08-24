package com.blog.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class UserServiceClientConfig {

    @Bean
    RestClient userServiceRestClient(
            UserServiceProperties properties) {

        return RestClient
                .builder()
                .baseUrl(properties.url())
                .build();
    }
}
