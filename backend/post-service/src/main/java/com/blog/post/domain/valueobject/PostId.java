package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public final class PostId {

    private UUID value;

    protected PostId() {
    }

    public static class UuidToStringConverter implements jakarta.persistence.AttributeConverter<UUID, String> {
        @Override
        public String convertToDatabaseColumn(UUID attribute) {
            return attribute == null ? null : attribute.toString();
        }

        @Override
        public UUID convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isBlank()) {
                return null;
            }
            return UUID.fromString(dbData.trim());
        }
    }

    public PostId(String value) {
        if (value == null) {
            throw new InvalidValueException("Post ID cannot be null");
        }
        try {
            this.value = UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            throw new InvalidValueException("Post ID must be a valid UUID");
        }
    }

    public PostId(UUID value) {
        if (value == null) {
            throw new InvalidValueException("Post ID cannot be null");
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
