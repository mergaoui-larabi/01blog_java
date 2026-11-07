import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environments';

export type ReportTargetType = 'USER' | 'POST';

export type ReportStatus = 'OPEN' | 'UNDER_REVIEW' | 'RESOLVED' | 'DISMISSED';

export interface Report {
  id: string;
  reporterId: string;
  targetType: ReportTargetType;
  targetId: string;
  reason: string;
  status: ReportStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReportRequest {
  userId: string;
  reason: string;
}

export interface PageResponse<T> {
  content: T[];
  nextCursor: string | null;
  hasNext: boolean;
}

export interface UpdateReportRequest {
  reason?: string;
  status?: ReportStatus;
}

@Service()
export class ReportApiService {
  private readonly http = inject(HttpClient);
  private readonly env = environment;

  createUserReport(request: CreateReportRequest): Observable<Report> {
    return this.http.post<Report>(this.env.createUserReportEndpoint, request);
  }

  createPostReport(request: CreateReportRequest): Observable<Report> {
    return this.http.post<Report>(this.env.createPostReportEndpoint, request);
  }

  fetchAdminReports(
    status?: ReportStatus | null,
    cursor?: string | null,
    size = 20,
  ): Observable<PageResponse<Report>> {
    let params = new HttpParams().set('size', size);
    if (status) {
      params = params.set('status', status);
    }
    if (cursor) {
      params = params.set('cursor', cursor);
    }

    return this.http.get<PageResponse<Report>>(this.env.getAdminReportsEndpoint, { params });
  }

  fetchReport(reportId: string): Observable<Report> {
    const url = this.env.getReportEndpoint.replace('{reportId}', encodeURIComponent(reportId));
    return this.http.get<Report>(url);
  }

  updateReport(reportId: string, request: UpdateReportRequest): Observable<Report> {
    const url = this.env.updateReportEndpoint.replace('{reportId}', encodeURIComponent(reportId));
    return this.http.patch<Report>(url, request);
  }

  deleteReport(reportId: string): Observable<void> {
    const url = this.env.deleteReportEndpoint.replace('{reportId}', encodeURIComponent(reportId));
    return this.http.delete<void>(url);
  }
}
