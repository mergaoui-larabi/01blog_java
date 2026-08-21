package com.blog.user.domain.valueobject;

import com.blog.user.constant.ValidationConstants;
import com.blog.user.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

@Embeddable
public final class Username {

    private String value;

    // Required by JPA
    protected Username() {
    }

    public Username(String value) {
        if (value == null) {
            throw new InvalidValueException("Username cannot be null");
        }
        value = value.trim();
        if (value.isBlank()) {
            throw new InvalidValueException("Username cannot be blank");
        }
        if (value.length() < ValidationConstants.USERNAME_MIN_LENGTH) {
            throw new InvalidValueException("Username must be at least "
                    + ValidationConstants.USERNAME_MIN_LENGTH
                    + " characters");
        }
        if (value.length() > ValidationConstants.USERNAME_MAX_LENGTH) {
            throw new InvalidValueException("Username must not exceed "
                    + ValidationConstants.USERNAME_MAX_LENGTH
                    + " characters");
        }
        if (!ValidationConstants.USERNAME_REGEX_PATTERN.matcher(value).matches()) {
            throw new InvalidValueException(
                    "Username can only contain letters, numbers, underscores and hyphens, and must start with a letter");
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