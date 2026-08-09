import { Component, input, output } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-report-modal',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './report-modal.html',
  styleUrl: './report-modal.css',
})
export class ReportModal {
  readonly userId = input.required<number>();
  readonly username = input('');
  readonly visible = input(false);

  readonly close = output<void>();
  readonly submitReport = output<{ reportedUserId: number; reason: string }>();

  reasonControl = new FormControl('', [Validators.required, Validators.minLength(10)]);

  submit(): void {
    if (this.reasonControl.invalid) return;
    this.submitReport.emit({
      reportedUserId: this.userId(),
      reason: this.reasonControl.value!,
    });
    this.reasonControl.reset();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.close.emit();
    }
  }
}
