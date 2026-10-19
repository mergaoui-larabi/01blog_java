import { Component, computed, inject, input, output, signal } from '@angular/core';
import { Dialog } from '../../../../shared/ui/dialog/dialog';
import { ReportRequest, ReportReason, REPORT_REASONS, ReportTarget } from '../../models/report.model';
import { ReportService } from '../../data-access/report.service';

/**
 * Report dialog for posts and user Blocks.
 *
 * Mock phase behavior: submission is recorded in local memory. The success
 * message deliberately avoids implying that anything has been reviewed or
 * that the reported entity has been actioned.
 */
@Component({
  selector: 'app-report-dialog',
  templateUrl: './report-dialog.html',
  imports: [Dialog],
})
export class ReportDialog {
  private readonly reportService = inject(ReportService);

  readonly target = input.required<ReportTarget>();

  readonly closed = output<void>();

  protected readonly reasons = REPORT_REASONS;

  protected readonly selectedReason = signal<ReportReason | null>(null);
  protected readonly details = signal('');
  protected readonly touched = signal(false);
  protected readonly submitting = signal(false);
  protected readonly succeeded = signal(false);
  protected readonly serverError = signal('');

  protected readonly targetDescription = computed(() => {
    const target = this.target();
    return target.type === 'POST' ? `Post: “${target.label}”` : `User: @${target.label}`;
  });

  protected readonly reasonError = computed(() =>
    this.touched() && !this.selectedReason() ? 'Please choose a reason.' : null,
  );

  protected selectReason(reason: ReportReason): void {
    this.selectedReason.set(reason);
    this.serverError.set('');
  }

  protected async submit(): Promise<void> {
    this.touched.set(true);

    const reason = this.selectedReason();
    if (!reason) {
      return;
    }

    this.submitting.set(true);
    this.serverError.set('');

    try {
      const request: ReportRequest = {
        target: this.target(),
        reason,
        details: this.details().trim(),
      };

      await this.reportService.submit(request);
      this.succeeded.set(true);
    } catch {
      this.serverError.set('Something went wrong while submitting the report. Please try again.');
    } finally {
      this.submitting.set(false);
    }
  }
}
