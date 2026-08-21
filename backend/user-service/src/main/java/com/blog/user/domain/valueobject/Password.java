package com.blog.user.domain.valueobject;

import com.blog.user.constant.ValidationConstants;
import com.blog.user.exception.InvalidValueException;

public final class Password {

    private final String value;

    public Password(String value) {
        if (value == null) {
            throw new InvalidValueException("Password cannot be null");
        }
        if (value.isBlank()) {
            throw new InvalidValueException("Password cannot be blank");
        }
        if (value.length() < ValidationConstants.PASSWORD_MIN_LENGTH) {
            throw new InvalidValueException(
                    "Password must be at least "
                            + ValidationConstants.PASSWORD_MIN_LENGTH
                            + " characters");
        }
        if (value.length() > ValidationConstants.PASSWORD_MAX_LENGTH) {
            throw new InvalidValueException(
                    "Password must not exceed "
                            + ValidationConstants.PASSWORD_MAX_LENGTH
                            + " characters");
        }
        if (!ValidationConstants.PASSWORD_REGEX_PATTERN.matcher(value).matches()) {
            throw new InvalidValueException(
                    "Password must contain at least one uppercase letter, "
                            + "one lowercase letter, one digit, and one special character, "
                            + "and must not contain whitespace");
        }

        this.value = value;
    }

    public String value() {
        return value;
    }
}