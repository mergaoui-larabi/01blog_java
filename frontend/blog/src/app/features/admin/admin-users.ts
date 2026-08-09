import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AdminService } from '../../core/services/admin.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.css',
})
export class AdminUsers implements OnInit {
  private readonly adminService = inject(AdminService);

  users: User[] = [];
  isLoading = true;
  message = '';

  ngOnInit(): void {
    this.loadUsers();
  }

  private loadUsers(): void {
    this.isLoading = true;
    this.adminService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  banUser(user: User): void {
    if (!confirm(`Ban user "${user.username}"? This cannot be undone.`)) return;

    this.adminService.banUser(user.id).subscribe({
      next: () => {
        user.isBanned = true;
        this.message = `User "${user.username}" has been banned.`;
      },
      error: () => {
        this.message = 'Failed to ban user.';
      },
    });
  }

  unbanUser(user: User): void {
    this.adminService.unbanUser(user.id).subscribe({
      next: () => {
        user.isBanned = false;
        this.message = `User "${user.username}" has been unbanned.`;
      },
      error: () => {
        this.message = 'Failed to unban user.';
      },
    });
  }

  deleteUser(user: User): void {
    if (!confirm(`Permanently delete user "${user.username}"? This cannot be undone!`)) return;

    this.adminService.deleteUser(user.id).subscribe({
      next: () => {
        this.users = this.users.filter((u) => u.id !== user.id);
        this.message = `User "${user.username}" deleted.`;
      },
      error: () => {
        this.message = 'Failed to delete user.';
      },
    });
  }

  getRoleClass(role: string): string {
    return role === 'ADMIN' ? 'role-admin' : 'role-user';
  }
}
