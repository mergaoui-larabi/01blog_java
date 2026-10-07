package com.blog.media.domain.valueobject;

import com.blog.media.constant.ValidationConstants;
import com.blog.media.exception.InvalidValueException;

import jakarta.persistence.Embeddable;

@Embeddable
public final class OriginalFilename {

    private String value;

    // Required by JPA
    protected OriginalFilename() {
    }

    public OriginalFilename(String value) {
        if (value == null) {
            throw new InvalidValueException("Original filename cannot be null");
        }
        value = value.trim();
        if (value.isBlank()) {
            throw new InvalidValueException("Original filename cannot be blank");
        }
        if (value.length() > ValidationConstants.ORIGINAL_FILENAME_MAX_LENGTH) {
            throw new InvalidValueException("Original filename must not exceed "
                    + ValidationConstants.ORIGINAL_FILENAME_MAX_LENGTH + " characters");
        }
        if (value.contains("/") || value.contains("\\") || value.contains("..")) {
            throw new InvalidValueException("Original filename must not contain path separators");
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
