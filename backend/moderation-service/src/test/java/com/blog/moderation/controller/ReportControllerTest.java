package com.blog.moderation.controller;

import com.blog.moderation.application.dto.request.CreatePostReportRequest;
import com.blog.moderation.application.dto.request.UpdateReportRequest;
import com.blog.moderation.application.dto.response.PageResponse;
import com.blog.moderation.application.dto.response.ReportResponse;
import com.blog.moderation.domain.model.ReportStatus;
import com.blog.moderation.domain.model.TargetType;
import com.blog.moderation.exception.AdminOnlyException;
import com.blog.moderation.exception.ReportHasActionsException;
import com.blog.moderation.exception.UserServiceUnavailableException;
import com.blog.moderation.service.AdminAuthorizationService;
import com.blog.moderation.service.ReportService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    private static final UUID REPORTER_ID = UUID.randomUUID();
    private static final UUID MODERATOR_ID = UUID.randomUUID();
    private static final UUID POST_ID = UUID.randomUUID();
    private static final UUID REPORT_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private AdminAuthorizationService adminAuthorizationService;

    @Test
    void anonymousUserCannotListReports() throws Exception {
        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reportService);
    }

    @Test
    void currentAdministratorListsReports() throws Exception {
        when(adminAuthorizationService.requireAdmin(any(Jwt.class))).thenReturn(MODERATOR_ID);
        when(reportService.fetchReports(eq(ReportStatus.OPEN), any(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(reportResponse()), null, false));

        mockMvc.perform(get("/api/v1/reports").param("status", "OPEN").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(REPORT_ID.toString()))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void ordinaryUserCannotListReports() throws Exception {
        when(adminAuthorizationService.requireAdmin(any(Jwt.class))).thenThrow(new AdminOnlyException());

        mockMvc.perform(get("/api/v1/reports").with(jwt()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reportService);
    }

    @Test
    void revokedAdministratorWithStaleAdminClaimIsDenied() throws Exception {
        when(adminAuthorizationService.requireAdmin(any(Jwt.class))).thenThrow(new AdminOnlyException());

        mockMvc.perform(delete("/api/v1/reports/" + REPORT_ID)
                        .with(jwt().jwt(token -> token.subject(REPORTER_ID.toString()).claims(claims -> claims.put("role", "ADMIN")))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reportService);
    }

    @Test
    void userServiceFailureDeniesModerationWork() throws Exception {
        when(adminAuthorizationService.requireAdmin(any(Jwt.class)))
                .thenThrow(new UserServiceUnavailableException("User Service is currently unavailable"));

        mockMvc.perform(get("/api/v1/reports").with(jwt()))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void authenticatedUserReportsPostWithItsOwnIdentity() throws Exception {
        when(reportService.createPostReport(any(CreatePostReportRequest.class), eq(REPORTER_ID)))
                .thenReturn(reportResponse());

        mockMvc.perform(post("/api/v1/reports/posts")
                        .with(jwt().jwt(token -> token.subject(REPORTER_ID.toString())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postId":"%s","reason":"spam","reporterId":"%s"}
                                """.formatted(POST_ID, UUID.randomUUID())))
                .andExpect(status().isCreated());

        ArgumentCaptor<UUID> reporter = ArgumentCaptor.forClass(UUID.class);
        verify(reportService).createPostReport(any(CreatePostReportRequest.class), reporter.capture());
        assertThat(reporter.getValue()).isEqualTo(REPORTER_ID);
    }

    @Test
    void reportWithBlankReasonIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/reports/users")
                        .with(jwt().jwt(token -> token.subject(REPORTER_ID.toString())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"%s","reason":"   "}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(reportService);
    }

    @Test
    void malformedReportIdIsRejected() throws Exception {
        mockMvc.perform(delete("/api/v1/reports/not-a-uuid").with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void administratorUpdatesReportStatus() throws Exception {
        when(adminAuthorizationService.requireAdmin(any(Jwt.class))).thenReturn(MODERATOR_ID);
        when(reportService.updateReport(eq(REPORT_ID), any(UpdateReportRequest.class), eq(MODERATOR_ID)))
                .thenReturn(reportResponse(ReportStatus.RESOLVED));

        mockMvc.perform(patch("/api/v1/reports/" + REPORT_ID)
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"RESOLVED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ReportStatus.RESOLVED.name()));
    }

    @Test
    void unknownStatusDoesNotReachTheReportService() throws Exception {
        mockMvc.perform(patch("/api/v1/reports/" + REPORT_ID)
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ARCHIVED"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }

    @Test
    void reportWithAuditHistoryCannotBeDeleted() throws Exception {
        when(adminAuthorizationService.requireAdmin(any(Jwt.class))).thenReturn(MODERATOR_ID);
        doThrow(new ReportHasActionsException())
                .when(reportService).deleteReport(REPORT_ID);

        mockMvc.perform(delete("/api/v1/reports/" + REPORT_ID).with(jwt()).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void administratorDeletesCleanReport() throws Exception {
        when(adminAuthorizationService.requireAdmin(any(Jwt.class))).thenReturn(MODERATOR_ID);

        mockMvc.perform(delete("/api/v1/reports/" + REPORT_ID).with(jwt()).with(csrf()))
                .andExpect(status().isNoContent());

        verify(reportService).deleteReport(REPORT_ID);
    }

    private ReportResponse reportResponse() {
        return reportResponse(ReportStatus.OPEN);
    }

    private ReportResponse reportResponse(ReportStatus status) {
        return new ReportResponse(REPORT_ID, REPORTER_ID, TargetType.POST, POST_ID,
                "spam", status, Instant.now(), Instant.now());
    }
}
