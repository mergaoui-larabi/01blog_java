package com.blog.auth.config;

import org.springframework.http.HttpMethod;

public class PublicRoute {
    private String path;
    private HttpMethod method;

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
