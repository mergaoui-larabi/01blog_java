package com.blog.gateway.security;

import org.springframework.security.access.AccessDeniedException;

public class AccountRestrictedException extends AccessDeniedException {

    private final String code;

    public AccountRestrictedException(String code) {
        super("Account access restricted");
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}