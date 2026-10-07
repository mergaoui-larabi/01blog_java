package com.blog.media.domain.valueobject;

import com.blog.media.domain.model.MediaPurpose;
import com.blog.media.exception.InvalidValueException;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public final class Purpose {

    @Enumerated(EnumType.STRING)
    private MediaPurpose value;

    // Required by JPA
    protected Purpose() {
    }

    public Purpose(MediaPurpose value) {
        if (value == null) {
            throw new InvalidValueException("Purpose cannot be null");
        }
        this.value = value;
    }

    public Purpose(String value) {
        if (value == null) {
            throw new InvalidValueException("Purpose cannot be null");
        }
        value = value.trim();
        if (value.isBlank()) {
            throw new InvalidValueException("Purpose cannot be blank");
        }
        try {
            this.value = MediaPurpose.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidValueException("Invalid purpose: " + value);
        }
    }

    public MediaPurpose value() {
        return value;
    }

    @Override
    public String toString() {
        return value == null ? null : value.name();
    }
}
