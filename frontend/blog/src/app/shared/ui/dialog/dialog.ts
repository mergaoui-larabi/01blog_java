import {
  Component,
  ElementRef,
  HostListener,
  computed,
  effect,
  input,
  output,
  viewChild,
} from '@angular/core';

/**
 * Accessible modal dialog:
 * - `role="dialog"` + `aria-modal` + labelled by its title
 * - Escape closes (unless disabled per-dialog)
 * - Backdrop click closes
 * - Focus moves into the dialog on open and is trapped with Tab
 * - Focus is restored to the trigger element on close
 *
 * Usage: render it conditionally (`@if`) so open/close is driven by the host.
 */
@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.html',
})
export class Dialog {
  readonly title = input.required<string>();
  /** Optional description rendered under the title and used as aria-describedby. */
  readonly description = input('');
  readonly closeOnEscape = input(true);
  readonly closeOnBackdrop = input(true);
  /** Max width utility class, e.g. 'max-w-lg'. */
  readonly maxWidth = input('max-w-lg');

  readonly closed = output<void>();

  protected readonly describedBy = computed(() => (this.description() ? 'dialog-description' : null));

  private readonly panel = viewChild<ElementRef<HTMLElement>>('panel');
  private readonly root = viewChild<ElementRef<HTMLElement>>('root');

  constructor() {
    effect(() => {
      const panelEl = this.panel()?.nativeElement;
      if (!panelEl) {
        return;
      }

      // Move focus into the dialog when it opens.
      const focusable = this.focusableElements();
      (focusable[0] ?? panelEl).focus();
    });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.closeOnEscape()) {
      this.closed.emit();
    }
  }

  onBackdrop(event: MouseEvent): void {
    if (this.closeOnBackdrop() && event.target === this.root()?.nativeElement) {
      this.closed.emit();
    }
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Tab') {
      return;
    }

    const focusable = this.focusableElements();
    if (focusable.length === 0) {
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    const active = document.activeElement;

    if (event.shiftKey && active === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && active === last) {
      event.preventDefault();
      first.focus();
    }
  }

  private focusableElements(): HTMLElement[] {
    const panelEl = this.panel()?.nativeElement;
    if (!panelEl) {
      return [];
    }

    return Array.from(
      panelEl.querySelectorAll<HTMLElement>(
        'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])',
      ),
    );
  }
}
