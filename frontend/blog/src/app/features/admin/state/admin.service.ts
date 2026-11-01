import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { AlertService } from '../../../core/alerts/alert.service';
import { Report, ReportApiService, ReportStatus } from '../../../shared/data-access/report.api.service';
import { UserSearch } from '../../explore/data-access/explore.api.service';
import { AdminApiService } from '../data-access/admin.api.service';

@Service()
export class AdminService {
  private readonly alertService = inject(AlertService);
  private readonly adminApi = inject(AdminApiService);
  private readonly reportApi = inject(ReportApiService);

  readonly users = signal<UserSearch[]>([]);
  readonly usersLoading = signal(false);
  readonly usersNextCursor = signal<string | null>(null);
  readonly usersHasMore = signal(false);
  readonly userSearchTerm = signal('');
  private readonly usersCursorInstant = signal<string | null>(null);

  readonly reports = signal<Report[]>([]);
  readonly reportsLoading = signal(false);
  readonly reportsNextCursor = signal<string | null>(null);
  readonly reportsHasMore = signal(false);
  readonly reportStatusFilter = signal<ReportStatus | null>(null);

  readonly hasUsers = computed(() => this.users().length > 0);
  readonly hasReports = computed(() => this.reports().length > 0);

  loadUsers(search?: string | null): void {
    const term = search?.trim() ?? '';
    this.userSearchTerm.set(term);
    this.fetchUsersPage(null, null, false);
  }

  loadMoreUsers(): void {
    if (this.usersLoading() || !this.usersHasMore()) return;

    this.fetchUsersPage(this.usersNextCursor(), this.usersCursorInstant(), true);
  }

  private fetchUsersPage(cursor: string | null, cursorInstant: string | null, append: boolean): void {
    if (this.usersLoading()) return;

    this.usersLoading.set(true);

    this.adminApi
      .fetchUsers(cursor, cursorInstant, this.userSearchTerm() || null)
      .pipe(
        catchError((error: unknown) => {
          this.usersHasMore.set(false);
          this.handleError(error, 'Failed to load users.');
          return EMPTY;
        }),
        finalize(() => this.usersLoading.set(false)),
      )
      .subscribe((page) => {
        this.users.update((current) => {
          if (!append) return page.content;

          const existingIds = new Set(current.map((user) => user.id));
          return [...current, ...page.content.filter((user) => !existingIds.has(user.id))];
        });

        const allUsers = this.users();
        const lastUser = allUsers[allUsers.length - 1];

        this.usersNextCursor.set(lastUser ? lastUser.id : null);
        this.usersCursorInstant.set(page.nextCursor ?? (lastUser ? lastUser.createdAt : null));
        this.usersHasMore.set(Boolean(page.nextCursor));
      });
  }

  banUser(userId: string): void {
    this.adminApi
      .banUser(userId)
      .pipe(catchError((error: unknown) => {
        this.handleError(error, 'Failed to ban user.');
        return EMPTY;
      }))
      .subscribe((result) => {
        this.setBanned(userId, result.banned);
        this.alertService.success('User banned.');
      });
  }

  unbanUser(userId: string): void {
    this.adminApi
      .unbanUser(userId)
      .pipe(catchError((error: unknown) => {
        this.handleError(error, 'Failed to unban user.');
        return EMPTY;
      }))
      .subscribe((result) => {
        this.setBanned(userId, result.banned);
        this.alertService.success('User unbanned.');
      });
  }

  deleteUser(userId: string): void {
    this.adminApi
      .deleteUser(userId)
      .pipe(catchError((error: unknown) => {
        this.handleError(error, 'Failed to delete user.');
        return EMPTY;
      }))
      .subscribe(() => {
        this.users.update((users) => users.filter((user) => user.id !== userId));
        this.alertService.success('User deleted.');
      });
  }

  private setBanned(userId: string, banned: boolean): void {
    this.users.update((users) =>
      users.map((user) => (user.id === userId ? { ...user, banned } : user)),
    );
  }

  loadReports(status?: ReportStatus | null): void {
    if (status !== undefined) {
      this.reportStatusFilter.set(status);
    }

    this.fetchReportsPage(null, false);
  }

  loadMoreReports(): void {
    if (this.reportsLoading() || !this.reportsHasMore()) return;

    this.fetchReportsPage(this.reportsNextCursor(), true);
  }

  private fetchReportsPage(cursor: string | null, append: boolean): void {
    if (this.reportsLoading()) return;

    this.reportsLoading.set(true);

    this.reportApi
      .fetchAdminReports(this.reportStatusFilter(), cursor)
      .pipe(
        catchError((error: unknown) => {
          this.reportsHasMore.set(false);
          this.handleError(error, 'Failed to load reports.');
          return EMPTY;
        }),
        finalize(() => this.reportsLoading.set(false)),
      )
      .subscribe((page) => {
        this.reports.update((current) => {
          if (!append) return page.content;

          const existingIds = new Set(current.map((report) => report.id));
          return [...current, ...page.content.filter((report) => !existingIds.has(report.id))];
        });

        this.reportsNextCursor.set(page.nextCursor ?? null);
        this.reportsHasMore.set(page.hasNext ?? Boolean(page.nextCursor));
      });
  }

  updateReportStatus(reportId: string, status: ReportStatus): void {
    this.reportApi
      .updateReport(reportId, { status })
      .pipe(catchError((error: unknown) => {
        this.handleError(error, 'Failed to update report.');
        return EMPTY;
      }))
      .subscribe((updated) => {
        const filter = this.reportStatusFilter();
        const noLongerMatchesFilter = Boolean(filter) && updated.status !== filter;

        this.reports.update((reports) => {
          if (noLongerMatchesFilter) {
            return reports.filter((report) => report.id !== reportId);
          }

          return reports.map((report) =>
            report.id === reportId ? { ...report, ...updated } : report,
          );
        });

        this.alertService.success(`Report marked ${updated.status.replace('_', ' ').toLowerCase()}.`);
      });
  }

  deleteReport(reportId: string): void {
    this.reportApi
      .deleteReport(reportId)
      .pipe(catchError((error: unknown) => {
        this.handleError(error, 'Failed to delete report.');
        return EMPTY;
      }))
      .subscribe(() => {
        this.reports.update((reports) => reports.filter((report) => report.id !== reportId));
        this.alertService.success('Report deleted.');
      });
  }

  private handleError(error: unknown, fallback: string): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        this.alertService.error('Cannot connect to the server.');
        return;
      }

      this.alertService.error(error.error?.message ?? fallback);
      return;
    }

    this.alertService.error('An unexpected error occurred. Please try again.');
  }
}
