package com.blog.media.domain.valueobject;

import com.blog.media.constant.ValidationConstants;
import com.blog.media.exception.InvalidValueException;

import jakarta.persistence.Embeddable;

@Embeddable
public final class StorageKey {

    private String value;

    // Required by JPA
    protected StorageKey() {
    }

    public StorageKey(String value) {
        if (value == null) {
            throw new InvalidValueException("Storage key cannot be null");
        }
        value = value.trim();
        if (value.isBlank()) {
            throw new InvalidValueException("Storage key cannot be blank");
        }
        if (value.length() > ValidationConstants.STORAGE_KEY_MAX_LENGTH) {
            throw new InvalidValueException("Storage key must not exceed "
                    + ValidationConstants.STORAGE_KEY_MAX_LENGTH + " characters");
        }
        if (!ValidationConstants.STORAGE_KEY_REGEX_PATTERN.matcher(value).matches()) {
            throw new InvalidValueException("Invalid storage key format");
        }
        if (value.contains("..")) {
            throw new InvalidValueException("Storage key must not contain path traversal");
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
