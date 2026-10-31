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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final UUID REPORTER_ID = UUID.randomUUID();
    private static final UUID MODERATOR_ID = UUID.randomUUID();
    private static final UUID POST_ID = UUID.randomUUID();
    private static final UUID TARGET_USER_ID = UUID.randomUUID();
    private static final UUID REPORT_ID = UUID.randomUUID();

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ModerationActionRepository moderationActionRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void createPostReportStoresReportAgainstPostWithAuthenticatedReporter() {
        givenSavedReport();

        ReportResponse response = reportService.createPostReport(
                new CreatePostReportRequest(POST_ID, " spam post "), REPORTER_ID);

        ArgumentCaptor<Report> saved = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(saved.capture());

        assertThat(saved.getValue().getReporterId()).isEqualTo(REPORTER_ID);
        assertThat(saved.getValue().getTargetType()).isEqualTo(TargetType.POST);
        assertThat(saved.getValue().getTargetId()).isEqualTo(POST_ID);
        assertThat(saved.getValue().getReason()).isEqualTo("spam post");
        assertThat(response.status()).isEqualTo(ReportStatus.OPEN);
        assertThat(response.targetType()).isEqualTo(TargetType.POST);
    }

    @Test
    void createUserReportStoresReportAgainstUserWithAuthenticatedReporter() {
        givenSavedReport();

        reportService.createUserReport(new CreateUserReportRequest(TARGET_USER_ID, "harassment"), REPORTER_ID);

        ArgumentCaptor<Report> saved = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(saved.capture());

        assertThat(saved.getValue().getTargetType()).isEqualTo(TargetType.USER);
        assertThat(saved.getValue().getTargetId()).isEqualTo(TARGET_USER_ID);
        assertThat(saved.getValue().getReporterId()).isEqualTo(REPORTER_ID);
    }

    @Test
    void updateReportChangesStatusAndRecordsAuditActionAtomically() throws Exception {
        Report report = openReport();
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
        when(reportRepository.save(report)).thenReturn(report);

        ReportResponse response = reportService.updateReport(
                REPORT_ID, new UpdateReportRequest(ReportStatus.RESOLVED), MODERATOR_ID);

        assertThat(response.status()).isEqualTo(ReportStatus.RESOLVED);

        ArgumentCaptor<ModerationAction> action = ArgumentCaptor.forClass(ModerationAction.class);
        verify(moderationActionRepository).save(action.capture());
        assertThat(action.getValue().getReportId()).isEqualTo(REPORT_ID);
        assertThat(action.getValue().getModeratorId()).isEqualTo(MODERATOR_ID);
        assertThat(action.getValue().getActionType()).isEqualTo(ModerationActionType.STATUS_CHANGED);
        assertThat(action.getValue().getDetails()).isEqualTo("OPEN -> RESOLVED");

        // The report update and its audit action must share one transaction boundary.
        assertThat(ReportService.class.getMethod(
                "updateReport", UUID.class, UpdateReportRequest.class, UUID.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    void updateMissingReportFailsWithoutRecordingAction() {
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.updateReport(
                REPORT_ID, new UpdateReportRequest(ReportStatus.DISMISSED), MODERATOR_ID))
                .isInstanceOf(ReportNotFoundException.class);

        verifyNoInteractions(moderationActionRepository);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void deleteRejectsReportWithModerationAuditHistory() {
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(openReport()));
        when(moderationActionRepository.existsByReportId(REPORT_ID)).thenReturn(true);

        assertThatThrownBy(() -> reportService.deleteReport(REPORT_ID))
                .isInstanceOf(ReportHasActionsException.class);

        verify(reportRepository, never()).delete(any(Report.class));
    }

    @Test
    void deleteRemovesReportWithoutAuditHistory() {
        Report report = openReport();
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
        when(moderationActionRepository.existsByReportId(REPORT_ID)).thenReturn(false);

        reportService.deleteReport(REPORT_ID);

        verify(reportRepository).delete(report);
    }

    @Test
    void deleteMissingReportFailsWithNotFound() {
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.deleteReport(REPORT_ID))
                .isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    void fetchReportsFirstPageReportsWhetherMorePagesRemain() {
        List<Report> reports = List.of(openReport(), openReport(), openReport());
        when(reportRepository.findFirstPage(any(Pageable.class))).thenReturn(reports);

        PageResponse<ReportResponse> page = reportService.fetchReports(null, null, 2);

        assertThat(page.content()).hasSize(2);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).isEqualTo(page.content().get(1).id().toString());
    }

    @Test
    void fetchReportsFiltersByStatusWithoutLoadingEverything() {
        when(reportRepository.findFirstPageByStatus(eq(ReportStatus.OPEN), any(Pageable.class)))
                .thenReturn(List.of(openReport()));

        PageResponse<ReportResponse> page = reportService.fetchReports(ReportStatus.OPEN, null, 2);

        assertThat(page.content()).hasSize(1);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.nextCursor()).isNull();
        verify(reportRepository, never()).findAll();
    }

    @Test
    void fetchReportsAfterCursorUsesKeysetQuery() {
        Report cursorReport = openReport();
        UUID cursorId = cursorReport.getId();
        when(reportRepository.findById(cursorId)).thenReturn(Optional.of(cursorReport));
        when(reportRepository.findWithCursor(eq(cursorId), any(Instant.class), any(Pageable.class)))
                .thenReturn(List.of(cursorReport));

        PageResponse<ReportResponse> page = reportService.fetchReports(null, cursorId, 2);

        assertThat(page.content()).hasSize(1);
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void fetchReportsAfterUnknownCursorFailsWithNotFound() {
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.fetchReports(null, REPORT_ID, 2))
                .isInstanceOf(ReportNotFoundException.class);
    }

    private void givenSavedReport() {
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Report openReport() {
        return reportWithId(UUID.randomUUID());
    }

    private Report reportWithId(UUID id) {
        Report report = new Report(REPORTER_ID, TargetType.POST, POST_ID, "spam");
        ReflectionTestUtils.setField(report, "id", id);
        return report;
    }
}
