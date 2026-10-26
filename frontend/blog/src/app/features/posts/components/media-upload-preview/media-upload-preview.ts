import { Component, OnDestroy, computed, input, output, signal } from '@angular/core';

interface MediaItem {
  id: string;
  file: File;
  previewUrl: string;
  type: 'image' | 'video';
}

@Component({
  selector: 'app-media-upload-preview',
  templateUrl: './media-upload-preview.html',
})
export class MediaUploadPreview implements OnDestroy {
  readonly uploadProgress = input<number | null>(null);
  readonly uploading = input(false);

  readonly filesChange = output<File[]>();
  readonly uploadCancelled = output<void>();

  readonly media = signal<MediaItem[]>([]);
  readonly activeIndex = signal(0);
  readonly isDragging = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly activeMedia = computed(() => this.media()[this.activeIndex()] ?? null);

  readonly mediaCount = computed(() => this.media().length);

  private readonly maxFiles = 10;
  private readonly maxFileSize = 10 * 1024 * 1024;

  addFiles(fileList: FileList | File[]): void {
    const incomingFiles = Array.from(fileList);
    const currentMedia = this.media();
    const remainingSlots = this.maxFiles - currentMedia.length;

    this.errorMessage.set(null);

    if (remainingSlots <= 0) {
      this.errorMessage.set(`You can attach up to ${this.maxFiles} files.`);
      return;
    }

    const accepted: MediaItem[] = [];
    const errors: string[] = [];

    for (const file of incomingFiles) {
      if (accepted.length >= remainingSlots) {
        errors.push(`You can attach up to ${this.maxFiles} files.`);
        break;
      }

      if (!this.isSupportedFile(file)) {
        errors.push(`${file.name}: only images and videos are supported.`);
        continue;
      }

      if (file.size > this.maxFileSize) {
        errors.push(`${file.name}: the maximum file size is 10 MB.`);
        continue;
      }

      accepted.push({
        id: crypto.randomUUID(),
        file,
        previewUrl: URL.createObjectURL(file),
        type: file.type.startsWith('image/') ? 'image' : 'video',
      });
    }

    if (accepted.length > 0) {
      this.media.update((items) => [...items, ...accepted]);
      this.activeIndex.set(this.media().length - 1);
      this.emitFiles();
    }

    if (errors.length > 0) {
      this.errorMessage.set(errors.join(' '));
    }
  }

  onFileInput(event: Event): void {
    const inputElement = event.target as HTMLInputElement;

    if (inputElement.files) {
      this.addFiles(inputElement.files);
    }

    // Allow the same file to be selected again.
    inputElement.value = '';
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging.set(true);
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();

    const currentTarget = event.currentTarget as HTMLElement;
    const relatedTarget = event.relatedTarget as Node | null;

    if (!relatedTarget || !currentTarget.contains(relatedTarget)) {
      this.isDragging.set(false);
    }
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging.set(false);

    if (event.dataTransfer?.files) {
      this.addFiles(event.dataTransfer.files);
    }
  }

  previous(): void {
    const count = this.mediaCount();

    if (count > 1) {
      this.activeIndex.update((index) => (index - 1 + count) % count);
    }
  }

  next(): void {
    const count = this.mediaCount();

    if (count > 1) {
      this.activeIndex.update((index) => (index + 1) % count);
    }
  }

  removeActive(): void {
    const current = this.media();
    const item = this.activeMedia();

    if (!item) {
      return;
    }

    URL.revokeObjectURL(item.previewUrl);

    const nextMedia = current.filter((media) => media.id !== item.id);

    this.media.set(nextMedia);
    this.activeIndex.set(
      nextMedia.length === 0 ? 0 : Math.min(this.activeIndex(), nextMedia.length - 1),
    );

    this.emitFiles();
  }

  cancelUpload(): void {
    if (this.uploading()) {
      this.uploadCancelled.emit();
    }
  }

  clear(): void {
    for (const item of this.media()) {
      URL.revokeObjectURL(item.previewUrl);
    }

    this.media.set([]);
    this.activeIndex.set(0);
    this.errorMessage.set(null);
    this.emitFiles();
  }

  ngOnDestroy(): void {
    for (const item of this.media()) {
      URL.revokeObjectURL(item.previewUrl);
    }
  }

  private isSupportedFile(file: File): boolean {
    return file.type.startsWith('image/') || file.type.startsWith('video/');
  }

  private emitFiles(): void {
    this.filesChange.emit(this.media().map((item) => item.file));
  }
}
