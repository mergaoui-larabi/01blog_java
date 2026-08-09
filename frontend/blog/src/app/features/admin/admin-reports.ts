import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AdminService } from '../../core/services/admin.service';
import { Report } from '../../core/models/report.model';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './admin-reports.html',
  styleUrl: './admin-reports.css',
})
export class AdminReports implements OnInit {
  private readonly adminService = inject(AdminService);

  reports: Report[] = [];
  isLoading = true;
  statusFilter: 'ALL' | 'PENDING' | 'RESOLVED' | 'DISMISSED' = 'ALL';
  message = '';

  ngOnInit(): void {
    this.loadReports();
  }

  private loadReports(): void {
    this.isLoading = true;
    this.adminService.getReports().subscribe({
      next: (reports) => {
        this.reports = reports;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  get filteredReports(): Report[] {
    if (this.statusFilter === 'ALL') return this.reports;
    return this.reports.filter((r) => r.status === this.statusFilter);
  }

  resolveReport(report: Report): void {
    this.adminService.resolveReport(report.id).subscribe({
      next: () => {
        report.status = 'RESOLVED';
        this.message = 'Report resolved successfully.';
      },
      error: () => {
        this.message = 'Failed to resolve report.';
      },
    });
  }

  dismissReport(report: Report): void {
    this.adminService.dismissReport(report.id).subscribe({
      next: () => {
        report.status = 'DISMISSED';
        this.message = 'Report dismissed.';
      },
      error: () => {
        this.message = 'Failed to dismiss report.';
      },
    });
  }

  banUser(report: Report): void {
    if (!confirm(`Ban user "${report.reportedUser.username}"? This cannot be undone.`)) return;

    this.adminService.banUser(report.reportedUser.id).subscribe({
      next: () => {
        this.message = `User ${report.reportedUser.username} has been banned.`;
        this.resolveReport(report);
      },
      error: () => {
        this.message = 'Failed to ban user.';
      },
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'status-pending';
      case 'RESOLVED': return 'status-resolved';
      case 'DISMISSED': return 'status-dismissed';
      default: return '';
    }
  }
}
