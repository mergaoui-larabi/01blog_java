package com.blog.media.domain.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

import com.blog.media.domain.model.MediaPurpose;
import com.blog.media.domain.model.MediaStatus;
import com.blog.media.domain.valueobject.DetectedContentType;
import com.blog.media.domain.valueobject.DurationMs;
import com.blog.media.domain.valueobject.Height;
import com.blog.media.domain.valueobject.OriginalFilename;
import com.blog.media.domain.valueobject.Purpose;
import com.blog.media.domain.valueobject.SizeBytes;
import com.blog.media.domain.valueobject.Status;
import com.blog.media.domain.valueobject.StorageKey;
import com.blog.media.domain.valueobject.Width;

@Entity
@Table(name = "medias")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "purpose", nullable = false, length = 30))
    private Purpose purpose;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "status", nullable = false, length = 30))
    private Status status;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "original_filename", nullable = false, length = 255))
    private OriginalFilename originalFilename;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "storage_key", nullable = false, unique = true, length = 500))
    private StorageKey storageKey;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "detected_content_type", nullable = false, length = 100))
    private DetectedContentType detectedContentType;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "size_bytes", nullable = false))
    private SizeBytes sizeBytes;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "width"))
    private Width width;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "height"))
    private Height height;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "duration_ms"))
    private DurationMs durationMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Required by JPA
    protected Media() {
    }

    public Media(
            UUID ownerId,
            Purpose purpose,
            Status status,
            OriginalFilename originalFilename,
            StorageKey storageKey,
            DetectedContentType detectedContentType,
            SizeBytes sizeBytes,
            Width width,
            Height height,
            DurationMs durationMs) {

        this.ownerId = ownerId;
        this.purpose = purpose;
        this.status = status;
        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.detectedContentType = detectedContentType;
        this.sizeBytes = sizeBytes;
        this.width = width;
        this.height = height;
        this.durationMs = durationMs;

        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // getters
    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public MediaPurpose getPurpose() {
        return purpose.value();
    }

    public MediaStatus getStatus() {
        return status.value();
    }

    public String getOriginalFilename() {
        return originalFilename.value();
    }

    public String getStorageKey() {
        return storageKey.value();
    }

    public String getDetectedContentType() {
        return detectedContentType.value();
    }

    public long getSizeBytes() {
        return sizeBytes.value();
    }

    public Integer getWidth() {
        return width == null ? null : width.value();
    }

    public Integer getHeight() {
        return height == null ? null : height.value();
    }

    public Long getDurationMs() {
        return durationMs == null ? null : durationMs.value();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // mutation helpers
    public void changePurpose(Purpose purpose) {
        this.purpose = purpose;
    }

    public void replaceFile(
            OriginalFilename originalFilename,
            StorageKey storageKey,
            DetectedContentType detectedContentType,
            SizeBytes sizeBytes,
            Width width,
            Height height,
            DurationMs durationMs) {

        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.detectedContentType = detectedContentType;
        this.sizeBytes = sizeBytes;
        this.width = width;
        this.height = height;
        this.durationMs = durationMs;
        touch();
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
