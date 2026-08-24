package com.blog.auth.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private List<PublicRoute> publicRoutes = new ArrayList<>();

    public List<PublicRoute> getPublicRoutes() {
        return publicRoutes;
    }

    public void setPublicRoutes(List<PublicRoute> publicRoutes) {
        this.publicRoutes = publicRoutes;
    }
}