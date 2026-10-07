package com.blog.media.domain.valueobject;

import com.blog.media.constant.ValidationConstants;
import com.blog.media.exception.InvalidValueException;

import jakarta.persistence.Embeddable;

@Embeddable
public final class SizeBytes {

    private long value;

    // Required by JPA
    protected SizeBytes() {
    }

    public SizeBytes(long value) {
        if (value < ValidationConstants.SIZE_BYTES_MIN) {
            throw new InvalidValueException("Size must be at least "
                    + ValidationConstants.SIZE_BYTES_MIN + " byte");
        }
        if (value > ValidationConstants.MAX_UPLOAD_SIZE_BYTES) {
            throw new InvalidValueException("Size must not exceed "
                    + ValidationConstants.MAX_UPLOAD_SIZE_BYTES + " bytes");
        }

        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
