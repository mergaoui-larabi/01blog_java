package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public final class AuthorId {

    private UUID value;

    protected AuthorId() {
    }

    public AuthorId(String value) {
        if (value == null) {
            throw new InvalidValueException("Author ID cannot be null");
        }
        try {
            this.value = UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("Author ID must be a valid UUID");
        }
    }

    public UUID value() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
