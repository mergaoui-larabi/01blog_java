import { inject, Service, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import {
  CreateReportRequest,
  Report,
  ReportApiService,
  ReportTargetType,
  UpdateReportRequest,
} from '../data-access/report.api.service';

const ACTIVE_STATUSES: Report['status'][] = ['OPEN', 'UNDER_REVIEW'];

@Service()
export class ReportService {
  private readonly reportApi = inject(ReportApiService);

  readonly reports = signal<Report[]>([]);
  readonly loading = signal(false);

  private loaded = false;

  load(): Observable<Report[]> {
    this.loading.set(true);

    return this.reportApi.fetchMyReports().pipe(
      tap({
        next: (reports) => {
          this.reports.set(reports);
          this.loaded = true;
        },
        complete: () => this.loading.set(false),
        error: () => this.loading.set(false),
      }),
    );
  }

  ensureLoaded(): void {
    if (this.loaded || this.loading()) return;

    this.load().subscribe({
      error: () => {
        // The button refetches on the next interaction; no user-facing noise here.
      },
    });
  }

  create(request: CreateReportRequest): Observable<Report> {
    return this.reportApi.createReport(request).pipe(
      tap((report) => this.reports.update((reports) => [report, ...reports])),
    );
  }

  update(reportId: string, request: UpdateReportRequest): Observable<Report> {
    return this.reportApi.updateReport(reportId, request).pipe(
      tap((report) =>
        this.reports.update((reports) =>
          reports.map((existing) => (existing.id === report.id ? report : existing)),
        ),
      ),
    );
  }

  delete(reportId: string): Observable<void> {
    return this.reportApi.deleteReport(reportId).pipe(
      tap(() =>
        this.reports.update((reports) => reports.filter((report) => report.id !== reportId)),
      ),
    );
  }

  activeReportFor(targetType: ReportTargetType, targetId: string): Report | null {
    return (
      this.reports().find(
        (report) =>
          report.targetType === targetType &&
          report.targetId === targetId &&
          ACTIVE_STATUSES.includes(report.status),
      ) ?? null
    );
  }
}
