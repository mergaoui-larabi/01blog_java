package com.blog.moderation.application.service;

import com.blog.moderation.domain.entity.ModerationAction;
import com.blog.moderation.domain.model.ActionType;
import com.blog.moderation.domain.model.ReportStatus;
import com.blog.moderation.domain.model.TargetType;
import com.blog.moderation.repository.ModerationActionRepository;
import com.blog.moderation.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ModerationService {

    private final ReportRepository reportRepository;
    private final ModerationActionRepository moderationActionRepository;

    public ModerationService(ReportRepository reportRepository,
                              ModerationActionRepository moderationActionRepository) {
        this.reportRepository = reportRepository;
        this.moderationActionRepository = moderationActionRepository;
    }

    @Transactional
    public ModerationAction createAction(UUID reportId, UUID moderatorId, ActionType actionType) {
        ModerationAction action = new ModerationAction(
                reportRepository.findById(reportId)
                        .orElseThrow(() -> new RuntimeException("Report not found")),
                moderatorId,
                actionType);
        return moderationActionRepository.save(action);
    }

    @Transactional(readOnly = true)
    public List<ModerationAction> getActionsByReportId(UUID reportId) {
        return moderationActionRepository.findByReportIdOrderByCreatedAtDesc(reportId);
    }

    @Transactional
    public void setReportStatus(UUID reportId, ReportStatus status) {
        reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"))
                .setStatus(status);
    }

    @Transactional(readOnly = true)
    public List<com.blog.moderation.domain.entity.Report> getReportsByStatus(ReportStatus status) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public List<com.blog.moderation.domain.entity.Report> getReportsByTargetType(TargetType targetType) {
        return reportRepository.findByTargetTypeOrderByCreatedAtDesc(targetType);
    }

    @Transactional(readOnly = true)
    public List<com.blog.moderation.domain.entity.Report> getReportsByTargetId(UUID targetId) {
        return reportRepository.findByTargetIdOrderByCreatedAtDesc(targetId);
    }
}
