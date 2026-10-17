package com.blog.moderation.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import com.blog.moderation.domain.model.ActionType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "moderation_actions")
public class ModerationAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "moderator_id", nullable = false)
    private UUID moderatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ActionType actionType;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ModerationAction() {
    }

    public ModerationAction(Report report, UUID moderatorId, ActionType actionType) {
        this.id = null;
        this.report = report;
        this.moderatorId = moderatorId;
        this.actionType = actionType;
        this.details = null;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Report getReport() {
        return report;
    }

    public UUID getModeratorId() {
        return moderatorId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public void setModeratorId(UUID moderatorId) {
        this.moderatorId = moderatorId;
    }

}
