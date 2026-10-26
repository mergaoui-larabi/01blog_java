import { Component, ElementRef, inject, output, signal, viewChild } from '@angular/core';
import { MediaUploadPreview } from '../media-upload-preview/media-upload-preview';
import { PostService } from '../../state/post.service';

@Component({
  selector: 'app-create-post-dialog',
  imports: [MediaUploadPreview],
  templateUrl: './create-post-dialog.html',
})
export class CreatePostDialog {
  readonly postService = inject(PostService);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  readonly content = signal('');
  readonly selectedFiles = signal<File[]>([]);
  readonly uploading = signal(false);
  readonly uploadProgress = signal<number | null>(null);
  readonly closed = output<void>();

  onFilesChange(files: File[]): void {
    this.selectedFiles.set(files);
  }

  onUploadCancelled(): void {
    // Connect this to the active HTTP upload request when
    // the media-service integration is implemented.
  }

  open(): void {
    this.dialog().nativeElement.showModal();
  }

  close(): void {
    this.dialog().nativeElement.close();
  }

  onClose(): void {
    this.closed.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.close();
    }
  }

  publishPost(): void {
    const content = this.content().trim();
    if (!content || this.uploading()) {
      return;
    }

    this.postService.createPost(content, this.selectedFiles());
  }
}
