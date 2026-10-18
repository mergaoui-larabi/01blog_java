package com.blog.post.controller;

import com.blog.post.domain.valueobject.UserId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal(expression = "jwt") Map<String, Object> claims) {
        String subject = (String) claims.get("sub");
        if (subject == null) {
            return ResponseEntity.badRequest().body("Missing subject in JWT");
        }

        UUID userId = UUID.fromString(subject);

        return ResponseEntity.ok(Map.of(
                "id", userId,
                "role", claims.get("role")
        ));
    }
}
