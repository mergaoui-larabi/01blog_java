import { Component, ElementRef, inject, output, signal, viewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Post } from '../../data-access/post.api.service';
import { PostApiService } from '../../data-access/post.api.service';
import { catchError, finalize } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { PostMediaGallery } from '../post-media-gallery/post-media-gallery';

@Component({
  selector: 'app-post-view-dialog',
  imports: [DatePipe, PostMediaGallery],
  templateUrl: './post-view-dialog.html',
  styleUrl: './post-view-dialog.scss',
})
export class PostViewDialog {
  private readonly postApi = inject(PostApiService);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  readonly post = signal<Post | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly closed = output<void>();

  open(post: Post): void {
    this.post.set(post);
    this.loading.set(false);
    this.error.set(null);
    this.dialog().nativeElement.showModal();
  }

  openById(postId: string): void {
    this.post.set(null);
    this.loading.set(true);
    this.error.set(null);
    this.dialog().nativeElement.showModal();

    this.postApi
      .fetchPost(postId)
      .pipe(
        catchError((err) => {
          this.error.set('Failed to load post.');
          return EMPTY;
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe((post) => {
        this.post.set(post);
      });
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

  isEdited(): boolean {
    const post = this.post();
    if (!post) return false;
    return new Date(post.updatedAt).getTime() > new Date(post.createdAt).getTime();
  }
}
