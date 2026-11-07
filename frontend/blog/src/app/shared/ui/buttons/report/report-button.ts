import { Component, computed, ElementRef, inject, input, signal, viewChild } from '@angular/core';
import { Report, ReportTargetType } from '../../../data-access/report.api.service';
import { ReportService } from '../../../state/report.service';
import { ConfirmDialog } from '../../confirm-dialog/confirm-dialog';
import { throttle } from '../../../../shared/utils/throttle';

@Component({
  selector: 'app-report-button',
  imports: [ConfirmDialog],
  templateUrl: './report-button.html',
  styleUrl: './report-button.scss',
})
export class ReportButton {
  readonly targetType = input.required<ReportTargetType>();
  readonly targetId = input.required<string>();
  readonly variant = input<'icon' | 'menu'>('icon');

  private readonly reportService = inject(ReportService);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  private readonly confirmDialog = viewChild.required(ConfirmDialog);

  readonly reason = signal('');
  readonly pending = signal(false);
  readonly existingReport = signal<Report | null>(null);

  /**
   * Throttled report-dialog opener so rapid repeated clicks cannot open the
   * dialog (and reload reports) multiple times. Runs at most once per 500 ms.
   */
  private readonly openThrottled = throttle(() => {
    this.reportService.ensureLoaded();
    const existing = this.reportService.activeReportFor(this.targetType(), this.targetId());

    this.existingReport.set(existing);
    this.reason.set(existing?.reason ?? '');
    this.pending.set(false);
    this.dialog().nativeElement.showModal();
  }, 500);

  readonly targetNoun = computed(() => (this.targetType() === 'POST' ? 'post' : 'user'));
  readonly hasReport = computed(() => this.existingReport() !== null);
  readonly editable = computed(() => this.existingReport()?.status === 'OPEN' || !this.hasReport());
  readonly statusLabel = computed(() =>
    this.existingReport() ? this.existingReport()!.status.replace('_', ' ').toLowerCase() : '',
  );

  onReasonInput(event: Event): void {
    this.reason.set((event.target as HTMLTextAreaElement).value);
  }

  open(event: MouseEvent): void {
    event.stopPropagation();
    this.openThrottled();
  }

  close(): void {
    this.dialog().nativeElement.close();
  }

  submit(): void {
    if (this.pending()) return;

    const reason = this.reason().trim();
    if (!reason) return;

    const existing = this.existingReport();
    const request$ = existing
      ? this.reportService.update(existing.id, { reason })
      : this.reportService.createReport(this.targetType(), {
          userId: this.targetId(),
          reason,
        });

    this.pending.set(true);
    request$.subscribe({
      next: (report) => {
        this.pending.set(false);
        this.existingReport.set(report as Report);
        this.close();
      },
      error: (error) => {
        this.pending.set(false);
        alert(this.getErrorMessage(error));
      },
    });
  }

  requestDelete(event: MouseEvent): void {
    event.stopPropagation();

    this.confirmDialog().open({
      title: 'Delete report',
      message: `This will remove your report of this ${this.targetNoun()}.`,
      confirmLabel: 'Delete',
      danger: true,
    });
  }

  confirmDelete(): void {
    const existing = this.existingReport();
    if (!existing || this.pending()) return;

    this.pending.set(true);
    this.reportService.delete(existing.id).subscribe({
      next: () => {
        this.pending.set(false);
        this.existingReport.set(null);
        this.reason.set('');
        this.close();
      },
      error: (error) => {
        this.pending.set(false);
        alert(this.getErrorMessage(error));
      },
    });
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.close();
    }
  }

  private getErrorMessage(error: unknown): string {
    const message = (error as { error?: { message?: string } })?.error?.message;

    return message ?? 'Something went wrong. Please try again.';
  }
}
