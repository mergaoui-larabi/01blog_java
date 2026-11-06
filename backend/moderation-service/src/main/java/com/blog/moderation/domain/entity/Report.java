package com.blog.moderation.domain.entity;

import com.blog.moderation.domain.model.ReportStatus;
import com.blog.moderation.domain.model.TargetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Nullable: cleared when the reporter deletes their account, the report is kept for audit
    @Column(name = "reporter_id")
    private UUID reporterId;

    @Column(name = "reporter_anonymized", nullable = false)
    private boolean reporterAnonymized = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status = ReportStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Report() {
    }

    public Report(UUID reporterId, TargetType targetType, UUID targetId, String reason) {
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public boolean isReporterAnonymized() {
        return reporterAnonymized;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getReason() {
        return reason;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public void anonymizeReporter() {
        this.reporterId = null;
        this.reporterAnonymized = true;
    }
}
