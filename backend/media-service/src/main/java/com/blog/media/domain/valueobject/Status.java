package com.blog.media.domain.valueobject;

import com.blog.media.domain.model.MediaStatus;
import com.blog.media.exception.InvalidValueException;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public final class Status {

    @Enumerated(EnumType.STRING)
    private MediaStatus value;

    // Required by JPA
    protected Status() {
    }

    public Status(MediaStatus value) {
        if (value == null) {
            throw new InvalidValueException("Status cannot be null");
        }
        this.value = value;
    }

    public Status(String value) {
        if (value == null) {
            throw new InvalidValueException("Status cannot be null");
        }
        value = value.trim();
        if (value.isBlank()) {
            throw new InvalidValueException("Status cannot be blank");
        }
        try {
            this.value = MediaStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidValueException("Invalid status: " + value);
        }
    }

    public MediaStatus value() {
        return value;
    }

    @Override
    public String toString() {
        return value == null ? null : value.name();
    }
}
