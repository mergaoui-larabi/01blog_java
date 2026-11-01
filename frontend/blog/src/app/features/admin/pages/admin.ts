import { Component, inject, signal, viewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Avatar } from '../../../shared/ui/avatar/avatar';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog/confirm-dialog';
import { Report, ReportStatus } from '../../../shared/data-access/report.api.service';
import { UserSearch } from '../../explore/data-access/explore.api.service';
import { AuthService } from '../../auth/state/auth.service';
import { AdminService } from '../state/admin.service';

type AdminTab = 'users' | 'reports';

interface PendingDelete {
  kind: 'user' | 'report';
  id: string;
  label: string;
}

@Component({
  selector: 'app-admin',
  imports: [Avatar, ConfirmDialog, DatePipe],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin {
  readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);

  protected readonly tab = signal<AdminTab>('users');
  protected readonly pendingDelete = signal<PendingDelete | null>(null);
  private readonly confirmDialog = viewChild.required(ConfirmDialog);

  protected readonly reportStatusOptions: ReportStatus[] = [
    'OPEN',
    'UNDER_REVIEW',
    'RESOLVED',
    'DISMISSED',
  ];

  ngOnInit(): void {
    this.adminService.loadUsers();
  }

  protected isSelf(userId: string): boolean {
    return this.authService.user()?.id === userId;
  }

  protected switchTab(tab: AdminTab): void {
    this.tab.set(tab);

    if (tab === 'reports' && !this.adminService.hasReports() && !this.adminService.reportsLoading()) {
      this.adminService.loadReports();
    }
  }

  protected searchUsers(term: string): void {
    this.adminService.loadUsers(term);
  }

  protected filterReports(value: string): void {
    const status = value ? (value as ReportStatus) : null;
    this.adminService.loadReports(status);
  }

  protected reportStatusText(status: ReportStatus): string {
    return status.replace('_', ' ');
  }

  protected confirmDeleteUser(user: UserSearch): void {
    this.pendingDelete.set({ kind: 'user', id: user.id, label: user.username });
    this.confirmDialog().open({
      title: 'Delete user',
      message: `Permanently delete @${user.username}? This cannot be undone.`,
      confirmLabel: 'Delete',
      danger: true,
    });
  }

  protected confirmDeleteReport(report: Report): void {
    this.pendingDelete.set({ kind: 'report', id: report.id, label: report.targetType });
    this.confirmDialog().open({
      title: 'Delete report',
      message: `Delete this ${report.targetType.toLowerCase()} report? This cannot be undone.`,
      confirmLabel: 'Delete',
      danger: true,
    });
  }

  protected onDeleteConfirmed(): void {
    const pending = this.pendingDelete();
    if (!pending) return;

    if (pending.kind === 'user') {
      this.adminService.deleteUser(pending.id);
    } else {
      this.adminService.deleteReport(pending.id);
    }

    this.pendingDelete.set(null);
  }
}
