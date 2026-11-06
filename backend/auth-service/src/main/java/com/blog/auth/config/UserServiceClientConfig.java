package com.blog.auth.config;

import com.blog.auth.constant.SecurityConstants;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class UserServiceClientConfig {

    @Bean
    RestClient userServiceRestClient(
            UserServiceProperties properties,
            InternalSecurityProperties internalProperties) {
        return RestClient
                .builder()
                .baseUrl(properties.url())
                .defaultHeader(SecurityConstants.INTERNAL_TOKEN_HEADER, internalProperties.token())
                .build();
    }
}
