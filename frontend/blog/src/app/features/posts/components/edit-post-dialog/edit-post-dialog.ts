import { Component, ElementRef, inject, output, signal, viewChild } from '@angular/core';
import { Post } from '../../data-access/post.api.service';
import { PostService } from '../../state/post.service';
import { MediaUploadPreview } from '../media-upload-preview/media-upload-preview';
import { MediaUrlService } from '../../../../shared/data-access/media-url.service';

@Component({
  selector: 'app-edit-post-dialog',
  imports: [MediaUploadPreview],
  templateUrl: './edit-post-dialog.html',
  styleUrl: './edit-post-dialog.scss',
})
export class EditPostDialog {
  private readonly postService = inject(PostService);
  readonly mediaUrls = inject(MediaUrlService);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  private readonly mediaPreview = viewChild.required(MediaUploadPreview);

  readonly saved = output<Post>();
  readonly closed = output<void>();

  readonly content = signal('');
  readonly existingIds = signal<string[]>([]);
  readonly selectedFiles = signal<File[]>([]);
  readonly saving = signal(false);
  readonly currentPost = signal<Post | null>(null);

  open(post: Post): void {
    this.currentPost.set(post);
    this.content.set(post.content);
    this.selectedFiles.set([]);
    this.existingIds.set([...post.mediaIds]);
    this.mediaPreview().clear();

    for (const mediaId of post.mediaIds) {
      this.mediaUrls.ensure(mediaId);
    }

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

  onFilesChange(files: File[]): void {
    this.selectedFiles.set(files);
  }

  removeExistingMedia(mediaId: string): void {
    this.existingIds.update((ids) => ids.filter((id) => id !== mediaId));
  }

  save(): void {
    const post = this.currentPost();
    const content = this.content().trim();

    if (!post || !content || this.saving()) {
      return;
    }

    this.saving.set(true);

    const remainingIds = this.existingIds();
    const mediaChanged = remainingIds.length !== post.mediaIds.length;

    this.postService
      .savePostEdits(post, content, this.selectedFiles(), remainingIds, mediaChanged)
      .subscribe({
        next: (updated) => {
          this.saving.set(false);
          this.saved.emit(updated);
          this.close();
        },
        error: () => {
          // The service already alerted; keep the dialog open so input is preserved.
          this.saving.set(false);
        },
      });
  }
}
