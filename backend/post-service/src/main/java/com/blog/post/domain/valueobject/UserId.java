package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;

import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new InvalidValueException("User id cannot be null");
        }
    }
}
