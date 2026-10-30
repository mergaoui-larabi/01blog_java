import { Component, computed, effect, inject, input, signal, OnDestroy } from '@angular/core';
import { NgClass } from '@angular/common';
import { firstValueFrom } from 'rxjs';

import { MediaApiService } from '../../data-access/media.api.service';
import { AuthService } from '../../../features/auth/state/auth.service';

export type AvatarSize = 'sm' | 'md' | 'lg' | 'xl';

@Component({
  selector: 'app-avatar',
  templateUrl: './avatar.html',
  styleUrl: './avatar.scss',
  imports: [NgClass],
})
export class Avatar implements OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly mediaApi = inject(MediaApiService);

  readonly mediaId = input<string | null | undefined>(undefined);
  readonly src = input<string | null>(null);
  readonly name = input<string | null>(null);
  readonly size = input<AvatarSize>('md');

  readonly imageFailed = signal(false);
  readonly avatarUrl = signal<string | null>(null);

  private objectUrl: string | null = null;
  private requestId = 0;

  readonly resolvedMediaId = computed(() => {
    const explicitMediaId = this.mediaId();

    if (explicitMediaId !== undefined) {
      return explicitMediaId;
    }

    return this.authService.getAuthenticatedUser()?.avatarMediaId ?? null;
  });

  readonly initials = computed(() => {
    const name = this.name()?.trim();

    if (!name) {
      return '?';
    }

    const parts = name.split(/\s+/);

    if (parts.length === 1) {
      return parts[0].slice(0, 2).toUpperCase();
    }

    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
  });

  constructor() {
    effect(() => {
      const src = this.src();
      const mediaId = this.resolvedMediaId();

      // A supplied URL takes precedence over the media ID.
      if (src) {
        this.loadImageUrl(src);
        return;
      }

      if (!mediaId) {
        this.invalidatePendingRequest();
        this.setAvatarUrl(null);
        return;
      }

      this.loadMediaAvatar(mediaId);
    });
  }

  onImageError(): void {
    this.imageFailed.set(true);
  }

  private async loadMediaAvatar(mediaId: string): Promise<void> {
    const requestId = ++this.requestId;

    try {
      const blob = await firstValueFrom(this.mediaApi.getMediaContent(mediaId));

      // Ignore responses from outdated requests.
      if (requestId !== this.requestId) {
        return;
      }

      this.setAvatarUrl(URL.createObjectURL(blob));
    } catch {
      if (requestId === this.requestId) {
        this.setAvatarUrl(null);
      }
    }
  }

  private loadImageUrl(src: string): void {
    this.invalidatePendingRequest();
    this.setAvatarUrl(src);
  }

  private invalidatePendingRequest(): void {
    this.requestId++;
  }

  private setAvatarUrl(url: string | null): void {
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }

    if (url?.startsWith('blob:')) {
      this.objectUrl = url;
    }

    this.imageFailed.set(false);
    this.avatarUrl.set(url);
  }

  ngOnDestroy(): void {
    this.invalidatePendingRequest();

    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }
  }
}
