package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;

import java.util.UUID;

public record PostId(UUID value) {

    public PostId {
        if (value == null) {
            throw new InvalidValueException("Post id cannot be null");
        }
    }
}
