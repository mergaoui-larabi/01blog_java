import { Injectable, signal } from '@angular/core';

export type AlertType = 'success' | 'error' | 'warning' | 'info';

export interface Alert {
  id: number;
  type: AlertType;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class AlertService {
  private readonly _alerts = signal<Alert[]>([]);
  readonly alerts = this._alerts.asReadonly();

  private nextId = 0;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();
  private readonly maxAlerts = 3;

  success(message: string, duration = 2500): void {
    this.show('success', message, duration);
  }

  error(message: string, duration = 4000): void {
    this.show('error', message, duration);
  }

  warning(message: string, duration = 4000): void {
    this.show('warning', message, duration);
  }

  info(message: string, duration = 2500): void {
    this.show('info', message, duration);
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);

    if (timer !== undefined) {
      clearTimeout(timer);
      this.timers.delete(id);
    }

    this._alerts.update((alerts) => alerts.filter((alert) => alert.id !== id));
  }

  private show(type: AlertType, message: string, duration: number): void {
    const id = ++this.nextId;

    // Remove the oldest alert if the limit has been reached.
    const currentAlerts = this._alerts();

    if (currentAlerts.length >= this.maxAlerts) {
      this.dismiss(currentAlerts[0].id);
    }

    this._alerts.update((alerts) => [...alerts, { id, type, message }]);

    if (duration > 0) {
      const timer = setTimeout(() => this.dismiss(id), duration);
      this.timers.set(id, timer);
    }
  }
}
