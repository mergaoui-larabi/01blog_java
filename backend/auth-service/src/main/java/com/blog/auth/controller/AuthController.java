package com.blog.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.auth.application.dto.request.LoginRequest;
import com.blog.auth.application.dto.response.LoginResponse;
import com.blog.auth.service.AuthenticationService;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> signin(
            @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);

        return ResponseEntity.ok(response);
    }

    // test if the service is up
    @GetMapping("ping")
    public String ping() {
        return "pong";
    }
}
