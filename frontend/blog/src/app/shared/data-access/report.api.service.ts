import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
  targetType: ReportTargetType;
  targetId: string;
  reason: string;
}

export interface UpdateReportRequest {
  reason?: string;
  status?: ReportStatus;
}

@Service()
export class ReportApiService {
  private readonly http = inject(HttpClient);
  private readonly env = environment;

  createReport(request: CreateReportRequest): Observable<Report> {
    return this.http.post<Report>(this.env.createReportEndpoint, request);
  }

  fetchMyReports(): Observable<Report[]> {
    return this.http.get<Report[]>(this.env.getMyReportsEndpoint);
  }

  fetchReport(reportId: string): Observable<Report> {
    const url = this.env.getReportEndpoint.replace(
      '{reportId}',
      encodeURIComponent(reportId),
    );
    return this.http.get<Report>(url);
  }

  updateReport(reportId: string, request: UpdateReportRequest): Observable<Report> {
    const url = this.env.updateReportEndpoint.replace(
      '{reportId}',
      encodeURIComponent(reportId),
    );
    return this.http.patch<Report>(url, request);
  }

  deleteReport(reportId: string): Observable<void> {
    const url = this.env.deleteReportEndpoint.replace(
      '{reportId}',
      encodeURIComponent(reportId),
    );
    return this.http.delete<void>(url);
  }
}
