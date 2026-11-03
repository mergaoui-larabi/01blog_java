import { Component, computed, inject, signal, viewChild } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
import { Avatar } from '../../../shared/ui/avatar/avatar';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog/confirm-dialog';
import { Report, ReportStatus } from '../../../shared/data-access/report.api.service';
import { UserSearch } from '../../explore/data-access/explore.api.service';
import { Post } from '../../posts/data-access/post.api.service';
import { AuthService } from '../../auth/state/auth.service';
import { AdminService } from '../state/admin.service';
import { PostSearchField } from '../data-access/admin.api.service';

type AdminTab = 'users' | 'posts' | 'reports';

interface PendingDelete {
  kind: 'user' | 'post' | 'report';
  id: string;
  label: string;
}

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
  protected readonly pendingDelete = signal<PendingDelete | null>(null);
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

  protected confirmDeleteUser(user: UserSearch): void {
    this.pendingDelete.set({ kind: 'user', id: user.id, label: user.username });
    this.confirmDialog().open({
      title: 'Delete user',
      message: `Permanently delete @${user.username}? This cannot be undone.`,
      confirmLabel: 'Delete',
      danger: true,
    });
  }

  protected confirmDeletePost(post: Post): void {
    this.pendingDelete.set({ kind: 'post', id: post.id, label: post.content.slice(0, 60) });
    this.confirmDialog().open({
      title: 'Delete post',
      message: 'Permanently delete this post? This cannot be undone.',
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
    } else if (pending.kind === 'post') {
      this.adminService.deletePost(pending.id);
    } else {
      this.adminService.deleteReport(pending.id);
    }

    this.pendingDelete.set(null);
  }
}
