import { Component, computed, inject, signal, viewChild } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
import { Avatar } from '../../../shared/ui/avatar/avatar';
import { ConfirmDialog, ConfirmDialogData } from '../../../shared/ui/confirm-dialog/confirm-dialog';
import { Report, ReportStatus } from '../../../shared/data-access/report.api.service';
import { UserSearch } from '../../explore/data-access/explore.api.service';
import { Post } from '../../posts/data-access/post.api.service';
import { AuthService } from '../../auth/state/auth.service';
import { AdminService } from '../state/admin.service';
import { PostSearchField } from '../data-access/admin.api.service';

type AdminTab = 'users' | 'posts' | 'reports';

@Component({
  selector: 'app-admin',
  imports: [Avatar, ConfirmDialog, DatePipe, SlicePipe],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin {
  readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);

  protected readonly tab = signal<AdminTab>('users');
  private readonly pendingAction = signal<(() => void) | null>(null);
  private readonly confirmDialog = viewChild.required(ConfirmDialog);

  protected readonly reportStatusOptions: ReportStatus[] = [
    'OPEN',
    'UNDER_REVIEW',
    'RESOLVED',
    'DISMISSED',
  ];

  protected readonly postFieldOptions: { value: PostSearchField; label: string }[] = [
    { value: 'all', label: 'All posts' },
    { value: 'postId', label: 'Post ID' },
    { value: 'authorId', label: 'Author ID' },
  ];

  protected readonly postSearchPlaceholder = computed(() => {
    const field = this.adminService.postFilter().field;

    if (field === 'postId') {
      return 'Paste a post ID...';
    }

    return field === 'authorId' ? 'Paste an author ID...' : 'Browse every post, hidden ones included.';
  });

  ngOnInit(): void {
    this.adminService.loadUsers();
  }

  protected isSelf(userId: string): boolean {
    return this.authService.user()?.id === userId;
  }

  protected switchTab(tab: AdminTab): void {
    this.tab.set(tab);

    if (tab === 'posts' && !this.adminService.hasPosts() && !this.adminService.postsLoading()) {
      this.adminService.loadPosts();
    }

    if (tab === 'reports' && !this.adminService.hasReports() && !this.adminService.reportsLoading()) {
      this.adminService.loadReports();
    }
  }

  protected searchUsers(term: string): void {
    this.adminService.loadUsers(term);
  }

  protected searchPosts(field: string, value: string): void {
    this.adminService.loadPosts({ field: field as PostSearchField, value: value.trim() });
  }

  protected filterReports(value: string): void {
    const status = value ? (value as ReportStatus) : null;
    this.adminService.loadReports(status);
  }

  protected reportStatusText(status: ReportStatus): string {
    return status.replace('_', ' ');
  }

  protected confirmBanUser(user: UserSearch): void {
    this.askConfirmation(
      {
        title: 'Ban user',
        message: `Ban @${user.username}? They will lose access until unbanned.`,
        confirmLabel: 'Ban',
        danger: true,
      },
      () => this.adminService.banUser(user.id),
    );
  }

  protected confirmUnbanUser(user: UserSearch): void {
    this.askConfirmation(
      {
        title: 'Unban user',
        message: `Restore access for @${user.username}? They will be able to sign in again.`,
        confirmLabel: 'Unban',
        danger: false,
      },
      () => this.adminService.unbanUser(user.id),
    );
  }

  protected confirmDeleteUser(user: UserSearch): void {
    this.askConfirmation(
      {
        title: 'Delete user',
        message: `Permanently delete @${user.username}? This cannot be undone.`,
        confirmLabel: 'Delete',
        danger: true,
      },
      () => this.adminService.deleteUser(user.id),
    );
  }

  protected confirmHidePost(post: Post): void {
    this.askConfirmation(
      {
        title: 'Hide post',
        message: `Hide "${this.preview(post)}"? Only its author will be able to see it.`,
        confirmLabel: 'Hide',
        danger: true,
      },
      () => this.adminService.hidePost(post.id),
    );
  }

  protected confirmUnhidePost(post: Post): void {
    this.askConfirmation(
      {
        title: 'Unhide post',
        message: `Make "${this.preview(post)}" visible again?`,
        confirmLabel: 'Unhide',
        danger: false,
      },
      () => this.adminService.unhidePost(post.id),
    );
  }

  protected confirmDeletePost(post: Post): void {
    this.askConfirmation(
      {
        title: 'Delete post',
        message: `Permanently delete "${this.preview(post)}"? This cannot be undone.`,
        confirmLabel: 'Delete',
        danger: true,
      },
      () => this.adminService.deletePost(post.id),
    );
  }

  protected confirmUpdateReportStatus(report: Report, status: ReportStatus): void {
    const statusText = this.reportStatusText(status).toLowerCase();

    this.askConfirmation(
      {
        title: `Mark report ${statusText}`,
        message: `Mark this ${report.targetType.toLowerCase()} report as ${statusText}?`,
        confirmLabel: this.reportActionLabel(status),
        danger: false,
      },
      () => this.adminService.updateReportStatus(report.id, status),
    );
  }

  protected confirmDeleteReport(report: Report): void {
    this.askConfirmation(
      {
        title: 'Delete report',
        message: `Delete this ${report.targetType.toLowerCase()} report? This cannot be undone.`,
        confirmLabel: 'Delete',
        danger: true,
      },
      () => this.adminService.deleteReport(report.id),
    );
  }

  protected onConfirmed(): void {
    const action = this.pendingAction();
    this.pendingAction.set(null);

    action?.();
  }

  private askConfirmation(request: ConfirmDialogData, action: () => void): void {
    this.pendingAction.set(action);
    this.confirmDialog().open(request);
  }

  private preview(post: Post): string {
    const content = post.content.replace(/\s+/g, ' ').trim();

    return content.length > 60 ? `${content.slice(0, 60)}...` : content;
  }

  private reportActionLabel(status: ReportStatus): string {
    if (status === 'RESOLVED') return 'Resolve';
    if (status === 'UNDER_REVIEW') return 'Review';

    return 'Dismiss';
  }
}
