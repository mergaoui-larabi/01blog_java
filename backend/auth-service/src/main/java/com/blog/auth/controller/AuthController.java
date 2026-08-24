package com.blog.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    // test if the service is up
    @GetMapping("ping")
    public String ping() {
        return "pong";
    }
}
