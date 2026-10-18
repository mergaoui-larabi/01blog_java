package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public final class UserId {

    private UUID value;

    protected UserId() {
    }

    public UserId(String value) {
        if (value == null) {
            throw new InvalidValueException("User ID cannot be null");
        }
        try {
            this.value = UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("User ID must be a valid UUID");
        }
    }

    public UserId(UUID value) {
        if (value == null) {
            throw new InvalidValueException("User ID cannot be null");
        }
        this.value = value;
    }

    public UUID value() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
