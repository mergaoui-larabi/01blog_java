import { Component, computed, effect, inject, input, OnDestroy, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { MediaApiService } from '../../../../shared/data-access/media.api.service';

interface MediaPreview {
  id: string;
  url: string;
  type: 'image' | 'video' | 'unknown';
}

@Component({
  selector: 'app-post-media-gallery',
  imports: [],
  templateUrl: './post-media-gallery.html',
  styleUrl: './post-media-gallery.scss',
})
export class PostMediaGallery implements OnDestroy {
  private readonly mediaApi = inject(MediaApiService);

  readonly mediaIds = input.required<string[]>();
  readonly activeIndex = signal(0);

  readonly mediaCount = computed(() => this.mediaIds().length);
  readonly activeMediaId = computed(() => this.mediaIds()[this.activeIndex()] ?? null);
  readonly mediaPreviews = signal<MediaPreview[]>([]);
  readonly loading = signal(false);

  private objectUrls: string[] = [];

  constructor() {
    effect(() => {
      const ids = this.mediaIds();
      this.loadMedia(ids);
    });
  }

  private async loadMedia(ids: string[]): Promise<void> {
    this.clearObjectUrls();
    this.mediaPreviews.set([]);
    if (ids.length === 0) {
      this.activeIndex.set(0);
      return;
    }

    this.loading.set(true);
    const previews: MediaPreview[] = [];

    for (const id of ids) {
      try {
        const blob = await firstValueFrom(this.mediaApi.getMediaContent(id));
        const url = URL.createObjectURL(blob);
        this.objectUrls.push(url);
        const type = blob.type.startsWith('video/') ? 'video' : blob.type.startsWith('image/') ? 'image' : blob.type === '' ? 'video' : 'unknown';
        previews.push({ id, url, type });
      } catch {
        // Skip failed media loads
      }
    }

    this.mediaPreviews.set(previews);
    this.activeIndex.set(0);
    this.loading.set(false);
  }

  readonly activePreview = computed(() => {
    const previews = this.mediaPreviews();
    const index = this.activeIndex();
    return previews[index] ?? null;
  });

  readonly activePreviewUrl = computed(() => {
    return this.activePreview()?.url ?? null;
  });

  readonly activePreviewType = computed(() => {
    return this.activePreview()?.type ?? 'unknown';
  });

  previous(): void {
    const count = this.mediaPreviews().length || this.mediaCount();
    if (count <= 1) return;
    this.activeIndex.update((index) => (index - 1 + count) % count);
  }

  next(): void {
    const count = this.mediaPreviews().length || this.mediaCount();
    if (count <= 1) return;
    this.activeIndex.update((index) => (index + 1) % count);
  }

  private clearObjectUrls(): void {
    for (const url of this.objectUrls) {
      if (url.startsWith('blob:')) {
        URL.revokeObjectURL(url);
      }
    }
    this.objectUrls = [];
  }

  ngOnDestroy(): void {
    this.clearObjectUrls();
  }
}
