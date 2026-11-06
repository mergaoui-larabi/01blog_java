package com.blog.moderation.service;

import com.blog.moderation.application.dto.response.UserCleanupResponse;
import com.blog.moderation.domain.entity.Report;
import com.blog.moderation.domain.model.ReportStatus;
import com.blog.moderation.domain.model.TargetType;
import com.blog.moderation.repository.ReportRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReportCleanupService {

    private final ReportRepository reportRepository;

    public ReportCleanupService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * Every step is driven by "does this row still reference the user", so a repeated cleanup
     * changes nothing and returns zero counts. Moderation actions are never touched: they are the
     * audit trail of what the moderators decided.
     */
    @Transactional
    public UserCleanupResponse cleanup(UUID userId) {
        List<Report> reportedByUser = reportRepository.findByReporterIdOrderByCreatedAtDesc(userId);
        reportedByUser.forEach(Report::anonymizeReporter);

        List<Report> targetingUser = reportRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                TargetType.USER, userId);
        long closed = targetingUser.stream()
                .filter(report -> report.getStatus() == ReportStatus.OPEN
                        || report.getStatus() == ReportStatus.UNDER_REVIEW)
                .peek(report -> report.setStatus(ReportStatus.DISMISSED))
                .count();

        reportRepository.saveAll(targetingUser);
        reportRepository.saveAll(reportedByUser);

        return new UserCleanupResponse(userId, reportedByUser.size(), closed);
    }
}
