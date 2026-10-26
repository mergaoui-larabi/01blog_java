import {
  Component,
  computed,
  effect,
  inject,
  input,
  signal,
  OnDestroy,
} from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { MediaApiService } from '../../data-access/media.api.service';

export type AvatarSize = 'sm' | 'md' | 'lg' | 'xl';

const SESSION_STORAGE_KEY = '01blog.session';

@Component({
  selector: 'app-avatar',
  templateUrl: './avatar.html',
})
export class Avatar implements OnDestroy {
  private readonly mediaApi = inject(MediaApiService);

  readonly src = input<string | null>(null);
  readonly name = input<string | null>(null);
  readonly size = input<AvatarSize>('md');

  readonly imageFailed = signal(false);
  readonly imageSrc = signal<string | null>(null);

  private objectUrl: string | null = null;
  private requestId = 0;

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

      // A supplied URL takes precedence over the session avatar.
      if (src) {
        this.setImageSource(src);
        return;
      }

      this.loadSessionAvatar();
    });
  }

  onImageError(): void {
    this.imageFailed.set(true);
  }

  private async loadSessionAvatar(): Promise<void> {
    const requestId = ++this.requestId;
    const mediaId = this.getAvatarMediaId();

    if (!mediaId) {
      this.setImageSource(null);
      return;
    }

    try {
      const blob = await firstValueFrom(
        this.mediaApi.getMediaContent(mediaId),
      );

      // Ignore responses from an older request.
      if (requestId !== this.requestId) {
        return;
      }

      this.setImageSource(URL.createObjectURL(blob));
    } catch {
      if (requestId === this.requestId) {
        this.setImageSource(null);
      }
    }
  }

  private setImageSource(src: string | null): void {
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }

    // Track object URLs so they can be revoked later.
    if (src?.startsWith('blob:')) {
      this.objectUrl = src;
    }

    this.imageFailed.set(false);
    this.imageSrc.set(src);
  }

  private getAvatarMediaId(): string | null {
    const session = sessionStorage.getItem(SESSION_STORAGE_KEY);

    if (!session) {
      return null;
    }

    try {
      const user = JSON.parse(session);
      return typeof user.avatarMediaId === 'string'
        ? user.avatarMediaId
        : null;
    } catch {
      return null;
    }
  }

  ngOnDestroy(): void {
    // Invalidate any outstanding request.
    this.requestId++;

    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
    }
  }
}