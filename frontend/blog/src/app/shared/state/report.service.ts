import { inject, output, Service, signal } from '@angular/core';
import { catchError, EMPTY, Observable, tap } from 'rxjs';
import {
  CreateReportRequest,
  Report,
  ReportApiService,
  ReportTargetType,
  UpdateReportRequest,
} from '../data-access/report.api.service';
import { AlertService } from '../../core/alerts/alert.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { TokenStorageService } from '../../features/auth/data-access/token-storage.service';

const ACTIVE_STATUSES: Report['status'][] = ['OPEN', 'UNDER_REVIEW'];

@Service()
export class ReportService {
  private readonly reportApi = inject(ReportApiService);
  private readonly alertService = inject(AlertService);
  private readonly router = inject(Router);
  private readonly tokenStorage = inject(TokenStorageService);

  readonly reports = signal<Report[]>([]);
  readonly loading = signal(false);

  private loaded = false;

  load(): void {
    this.loading.set(true);
  }

  ensureLoaded(): void {
    if (this.loaded || this.loading()) return;
  }

  createReport(targetType: ReportTargetType, request: CreateReportRequest): Observable<Report> {
    if (targetType === 'USER') {
      return this.reportApi.createUserReport(request).pipe(
        tap((report) => {
          this.alertService.success('Report created successfully.');
          this.reports.update((reports) => [report, ...reports]);
        }),
      );
    }
    return this.reportApi.createPostReport(request).pipe(
      tap((report) => {
        this.alertService.success('Report created successfully.');
        this.reports.update((reports) => [report, ...reports]);
      }),
      catchError((err: unknown) => {
        this.getErrorMessage(err);
        return EMPTY;
      }),
    );
  }

  update(reportId: string, request: UpdateReportRequest): Observable<Report> {
    return this.reportApi.updateReport(reportId, request).pipe(
      tap((report) => {
        this.alertService.success('report updated successfully.');
        this.reports.update((reports) =>
          reports.map((existing) => (existing.id === report.id ? report : existing)),
        );
      }),
      catchError((err: unknown) => {
        this.getErrorMessage(err);
        return EMPTY;
      }),
    );
  }

  delete(reportId: string): Observable<void> {
    return this.reportApi.deleteReport(reportId).pipe(
      tap(() => {
        this.alertService.success('report deleted successfully.');
        this.reports.update((reports) => reports.filter((report) => report.id !== reportId));
      }),
      catchError((err: unknown) => {
        this.getErrorMessage(err);
        return EMPTY;
      }),
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

  getErrorMessage(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 401) {
        this.tokenStorage.clearToken();
        void this.router.navigate(['/auth/login']);
      }

      if (error.status === 403) {
        console.log('Authorization error:', error);
        const errorCode = error.error?.code;

        switch (errorCode) {
          case 'ACCOUNT_BANNED':
            this.tokenStorage.clearToken();
            void this.router.navigate(['/auth/login']);
            break;

          case 'ACCOUNT_DELETED':
            this.tokenStorage.clearToken();
            void this.router.navigate(['/auth/login']);
            break;

          default:
            break;
        }
      }

      if (error.status === 0) {
        this.alertService.error('Cannot connect to the server.');
        return;
      }

      const message = error.error?.message;
      this.alertService.error(
        typeof message === 'string' ? message : 'Something went wrong. Please try again.',
      );
      return;
    }

    this.alertService.error('An unexpected error occurred. Please try again.');
  }
}
