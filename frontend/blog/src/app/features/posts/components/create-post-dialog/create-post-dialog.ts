import { Component, ElementRef, inject, output, signal, viewChild } from '@angular/core';
import { MediaUploadPreview } from '../media-upload-preview/media-upload-preview';
import { PostService } from '../../state/post.service';
import { Post } from '../../data-access/post.api.service';
import { finalize, pipe } from 'rxjs';

@Component({
  selector: 'app-create-post-dialog',
  imports: [MediaUploadPreview],
  templateUrl: './create-post-dialog.html',
  styleUrl: './create-post-dialog.scss',
})
export class CreatePostDialog {
  readonly postService = inject(PostService);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  private readonly mediaPreview = viewChild.required(MediaUploadPreview);

  readonly content = signal('');
  readonly selectedFiles = signal<File[]>([]);
  readonly uploading = signal(false);
  readonly uploadProgress = signal<number | null>(null);
  readonly closed = output<void>();
  readonly published = output<Post | null>();

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
    this.uploading.set(true);

    this.postService
      .createPost(content, this.selectedFiles())
      .pipe(finalize(() => this.uploading.set(false)))
      .subscribe((post) => {
        this.published.emit(post);
        this.mediaPreview().clear();
        this.selectedFiles.set([]);
        this.content.set('');
        this.close();
      });
  }
}
