import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AdminService } from '../../core/services/admin.service';
import { Post } from '../../core/models/post.model';
import { User } from '../../core/models/user.model';
import { Report } from '../../core/models/report.model';
import { AdminUsers } from './admin-users';
import { AdminReports } from './admin-reports';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [DatePipe, AdminUsers, AdminReports],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit {
  private readonly adminService = inject(AdminService);

  users: User[] = [];
  posts: Post[] = [];
  reports: Report[] = [];
  isLoading = true;
  message = '';
  activeTab: 'overview' | 'users' | 'reports' = 'overview';

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll(): void {
    this.isLoading = true;
    this.adminService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.adminService.getPosts().subscribe({
          next: (posts) => {
            this.posts = posts;
            this.adminService.getReports().subscribe({
              next: (reports) => {
                this.reports = reports;
                this.isLoading = false;
              },
            });
          },
        });
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  deletePost(post: Post): void {
    if (!confirm(`Delete post "${post.title}"?`)) return;
    this.adminService.deletePost(post.id).subscribe({
      next: () => {
        this.posts = this.posts.filter((p) => p.id !== post.id);
        this.message = 'Post deleted.';
      },
      error: () => {
        this.message = 'Failed to delete post.';
      },
    });
  }

  get pendingReportsCount(): number {
    return this.reports.filter((r) => r.status === 'PENDING').length;
  }

  get bannedUsersCount(): number {
    return this.users.filter((u) => u.isBanned).length;
  }
}
