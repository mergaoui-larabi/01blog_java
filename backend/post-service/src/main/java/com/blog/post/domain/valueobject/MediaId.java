package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

@Embeddable
public final class MediaId {

    private String value;

    protected MediaId() {
    }

    public MediaId(String value) {
        if (value == null) {
            throw new InvalidValueException("Media ID cannot be null");
        }
        if (value.isBlank()) {
            throw new InvalidValueException("Media ID cannot be blank");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
