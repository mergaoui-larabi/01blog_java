import { inject, Service, signal } from '@angular/core';

import { MediaApiService } from './media.api.service';

export type MediaKind = 'image' | 'video';

interface CachedMedia {
  url: string;
  kind: MediaKind;
}

/**
 * Caches object URLs per media id so a media file fetched once (card,
 * dialog, gallery) is never downloaded again while the app is open.
 */
@Service()
export class MediaUrlService {
  private readonly mediaApi = inject(MediaApiService);

  private readonly cache = signal<Record<string, CachedMedia>>({});
  readonly media = this.cache.asReadonly();

  private readonly failed = new Set<string>();
  private readonly pending = new Set<string>();

  ensure(mediaId: string): void {
    if (this.cache()[mediaId] || this.pending.has(mediaId) || this.failed.has(mediaId)) {
      return;
    }

    this.pending.add(mediaId);

    this.mediaApi.getMediaContent(mediaId).subscribe({
      next: (blob) => {
        this.pending.delete(mediaId);

        const kind: MediaKind = blob.type.startsWith('video/') ? 'video' : 'image';
        this.cache.update((entries) => ({
          ...entries,
          [mediaId]: { url: URL.createObjectURL(blob), kind },
        }));
      },
      error: () => {
        this.pending.delete(mediaId);
        this.failed.add(mediaId);
      },
    });
  }

  urlOf(mediaId: string): string | null {
    return this.cache()[mediaId]?.url ?? null;
  }

  kindOf(mediaId: string): MediaKind | null {
    return this.cache()[mediaId]?.kind ?? null;
  }

  hasFailed(mediaId: string): boolean {
    return this.failed.has(mediaId);
  }
}
