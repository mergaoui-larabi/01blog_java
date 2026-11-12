package com.blog.user.bootstrap;

import com.blog.user.config.AdminBootstrapProperties;
import com.blog.user.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final AdminBootstrapProperties properties;
    private final UserService userService;

    public AdminUserInitializer(
            AdminBootstrapProperties properties,
            UserService userService) {
        this.properties = properties;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }

        if (properties.email() == null || properties.email().isBlank()
                || properties.username() == null || properties.username().isBlank()
                || properties.password() == null || properties.password().isBlank()) {
            throw new IllegalStateException(
                    "Admin bootstrap is enabled, but credentials are incomplete");
        }
        userService.createAdmin(
                properties.email(),
                properties.username(),
                properties.password(),
                properties.age(),
                properties.gender());
    }
}