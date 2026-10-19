import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formats an ISO timestamp as a compact relative time ("3h ago").
 * Falls back to an absolute date beyond a week.
 */
@Pipe({ name: 'timeAgo', standalone: true, pure: true })
export class TimeAgoPipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    if (!value) {
      return '';
    }

    const date = typeof value === 'string' ? new Date(value) : value;

    if (Number.isNaN(date.getTime())) {
      return '';
    }

    const diffMs = Date.now() - date.getTime();
    const minutes = Math.round(diffMs / 60_000);

    if (minutes < 1) {
      return 'just now';
    }
    if (minutes < 60) {
      return `${minutes}m ago`;
    }

    const hours = Math.round(minutes / 60);
    if (hours < 24) {
      return `${hours}h ago`;
    }

    const days = Math.round(hours / 24);
    if (days <= 7) {
      return `${days}d ago`;
    }

    return date.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  }
}
