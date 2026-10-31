package com.blog.moderation.service;

import com.blog.moderation.application.dto.request.CreatePostReportRequest;
import com.blog.moderation.application.dto.request.CreateUserReportRequest;
import com.blog.moderation.application.dto.request.UpdateReportRequest;
import com.blog.moderation.application.dto.response.PageResponse;
import com.blog.moderation.application.dto.response.ReportResponse;
import com.blog.moderation.domain.entity.ModerationAction;
import com.blog.moderation.domain.entity.Report;
import com.blog.moderation.domain.model.ModerationActionType;
import com.blog.moderation.domain.model.ReportStatus;
import com.blog.moderation.domain.model.TargetType;
import com.blog.moderation.exception.ReportHasActionsException;
import com.blog.moderation.exception.ReportNotFoundException;
import com.blog.moderation.repository.ModerationActionRepository;
import com.blog.moderation.repository.ReportRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ModerationActionRepository moderationActionRepository;

    public ReportService(ReportRepository reportRepository,
            ModerationActionRepository moderationActionRepository) {
        this.reportRepository = reportRepository;
        this.moderationActionRepository = moderationActionRepository;
    }

    @Transactional
    public ReportResponse createPostReport(CreatePostReportRequest request, UUID reporterId) {
        Report report = new Report(reporterId, TargetType.POST, request.postId(), request.reason().trim());
        return ReportResponse.from(reportRepository.save(report));
    }

    @Transactional
    public ReportResponse createUserReport(CreateUserReportRequest request, UUID reporterId) {
        Report report = new Report(reporterId, TargetType.USER, request.userId(), request.reason().trim());
        return ReportResponse.from(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> fetchReports(ReportStatus status, UUID cursor, int size) {
        Pageable pageable = PageRequest.ofSize(size + 1);
        List<Report> reports;

        if (cursor == null) {
            reports = status != null
                    ? reportRepository.findFirstPageByStatus(status, pageable)
                    : reportRepository.findFirstPage(pageable);
        } else {
            Instant cursorCreatedAt = requireReport(cursor).getCreatedAt();
            reports = status != null
                    ? reportRepository.findWithCursorByStatus(status, cursor, cursorCreatedAt, pageable)
                    : reportRepository.findWithCursor(cursor, cursorCreatedAt, pageable);
        }

        return buildPageResponse(reports, size);
    }

    @Transactional
    public ReportResponse updateReport(UUID reportId, UpdateReportRequest request, UUID moderatorId) {
        Report report = requireReport(reportId);
        ReportStatus previousStatus = report.getStatus();
        ReportStatus newStatus = request.status();

        report.setStatus(newStatus);
        Report updated = reportRepository.save(report);

        moderationActionRepository.save(new ModerationAction(
                reportId, moderatorId, ModerationActionType.STATUS_CHANGED,
                previousStatus + " -> " + newStatus));

        return ReportResponse.from(updated);
    }

    @Transactional
    public void deleteReport(UUID reportId) {
        Report report = requireReport(reportId);

        if (moderationActionRepository.existsByReportId(reportId)) {
            throw new ReportHasActionsException();
        }

        reportRepository.delete(report);
    }

    private Report requireReport(UUID reportId) {
        return reportRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);
    }

    private PageResponse<ReportResponse> buildPageResponse(List<Report> reports, int size) {
        boolean hasNext = reports.size() > size;
        List<Report> contentReports = hasNext ? reports.subList(0, size) : reports;
        String nextCursor = hasNext && !contentReports.isEmpty()
                ? contentReports.get(contentReports.size() - 1).getId().toString()
                : null;

        List<ReportResponse> responses = contentReports.stream()
                .map(ReportResponse::from)
                .toList();

        return new PageResponse<>(responses, nextCursor, hasNext);
    }
}
