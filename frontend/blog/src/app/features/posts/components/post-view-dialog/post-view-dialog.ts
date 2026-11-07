import { Component, ElementRef, computed, inject, output, signal, viewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Comment, Post, PostApiService } from '../../data-access/post.api.service';
import { catchError, finalize } from 'rxjs/operators';
import { EMPTY } from 'rxjs';
import { PostMediaGallery } from '../post-media-gallery/post-media-gallery';
import { PostService } from '../../state/post.service';
import { PostAuthorService } from '../../state/post-author.service';
import { Avatar } from '../../../../shared/ui/avatar/avatar';
import { TimeAgoPipe } from '../../../../shared/pipes/time-ago.pipe';
import { ConfirmDialog } from '../../../../shared/ui/confirm-dialog/confirm-dialog';
import { AuthService } from '../../../auth/state/auth.service';
import { Logo } from '../../../../shared/components/logo/logo';
import { DeadLogo } from '../../../../shared/components/dead-logo/dead-logo';
import { throttle } from '../../../../shared/utils/throttle';

@Component({
  selector: 'app-post-view-dialog',
  imports: [DatePipe, RouterLink, TimeAgoPipe, Avatar, PostMediaGallery, ConfirmDialog, DeadLogo],
  templateUrl: './post-view-dialog.html',
  styleUrl: './post-view-dialog.scss',
})
export class PostViewDialog {
  private readonly postApi = inject(PostApiService);
  private readonly postService = inject(PostService);
  private readonly authorService = inject(PostAuthorService);
  private readonly authService = inject(AuthService);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  private readonly confirmDialog = viewChild.required(ConfirmDialog);

  readonly post = signal<Post | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly comments = signal<Comment[]>([]);
  readonly commentsLoading = signal(false);
  readonly commentsLoadingMore = signal(false);
  readonly commentsError = signal<string | null>(null);
  readonly commentsNextCursor = signal<string | null>(null);
  readonly commentsHasNext = signal(false);

  readonly newCommentContent = signal('');
  readonly submittingComment = signal(false);

  readonly editingCommentId = signal<string | null>(null);
  readonly editContent = signal('');
  readonly savingCommentId = signal<string | null>(null);
  readonly deletingCommentId = signal<string | null>(null);

  private pendingCommentDeleteId: string | null = null;

  readonly closed = output<void>();

  readonly currentUserId = computed(() => this.authService.user()?.id ?? null);

  readonly likePending = computed(() => {
    const post = this.post();
    return post ? !!this.postService.pendingLikes()[post.id] : false;
  });

  /**
   * Throttled like toggle so rapid repeated clicks cannot fire multiple
   * like/unlike requests. Runs at most once per 1000 ms.
   */
  private readonly likeThrottled = throttle(() => {
    const post = this.post();
    if (!post) {
      return;
    }

    this.postService.toggleLike(post, (updated) => {
      if (this.post()?.id === updated.id) {
        this.post.set(updated);
      }
    });
  }, 1000);

  open(post: Post): void {
    this.post.set(post);
    this.loading.set(false);
    this.error.set(null);
    this.resetComments();
    this.dialog().nativeElement.showModal();
    this.loadComments();
  }

  openById(postId: string): void {
    this.post.set(null);
    this.loading.set(true);
    this.error.set(null);
    this.resetComments();
    this.dialog().nativeElement.showModal();

    this.postApi
      .fetchPost(postId)
      .pipe(
        catchError(() => {
          this.error.set('Failed to load post.');
          return EMPTY;
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe((post) => {
        this.post.set(post);
        this.loadComments();
      });
  }

  close(): void {
    this.dialog().nativeElement.close();
  }

  onClose(): void {
    this.editingCommentId.set(null);
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

  authorOf(userId: string): ReturnType<PostAuthorService['authorOf']> {
    this.authorService.load(userId);
    return this.authorService.authorOf(userId);
  }

  toggleLike(): void {
    const post = this.post();

    if (!post) {
      return;
    }

    this.likeThrottled();
  }

  // ---------------------------------------------------------------------------
  // Comments
  // ---------------------------------------------------------------------------

  private resetComments(): void {
    this.comments.set([]);
    this.commentsNextCursor.set(null);
    this.commentsHasNext.set(false);
    this.commentsError.set(null);
    this.commentsLoading.set(false);
    this.commentsLoadingMore.set(false);
    this.newCommentContent.set('');
    this.submittingComment.set(false);
  }

  loadComments(): void {
    const post = this.post();

    if (!post || this.commentsLoading()) {
      return;
    }

    this.commentsLoading.set(true);
    this.commentsError.set(null);

    this.postApi
      .fetchComments(post.id)
      .pipe(finalize(() => this.commentsLoading.set(false)))
      .subscribe({
        next: (page) => {
          this.comments.set(page.content);
          this.commentsNextCursor.set(page.nextCursor);
          this.commentsHasNext.set(page.hasNext);
        },
        error: () => {
          this.commentsError.set('Failed to load comments.');
        },
      });
  }

  loadMoreComments(): void {
    const post = this.post();
    const cursor = this.commentsNextCursor();

    if (!post || !cursor || !this.commentsHasNext()) {
      return;
    }

    if (this.commentsLoadingMore()) {
      return;
    }

    this.commentsLoadingMore.set(true);

    this.postApi
      .fetchComments(post.id, cursor)
      .pipe(finalize(() => this.commentsLoadingMore.set(false)))
      .subscribe({
        next: (page) => {
          const existing = new Set(this.comments().map((comment) => comment.id));

          this.comments.update((comments) => [
            ...comments,
            ...page.content.filter((comment) => !existing.has(comment.id)),
          ]);
          this.commentsNextCursor.set(page.nextCursor);
          this.commentsHasNext.set(page.hasNext);
        },
        error: () => {
          this.postService.getErrorMessage('An unexpected error occurred. Please try again.');
        },
      });
  }

  addComment(): void {
    const post = this.post();
    const content = this.newCommentContent().trim();

    if (!post || !content || this.submittingComment()) {
      return;
    }

    this.submittingComment.set(true);

    this.postApi.createComment(post.id, { content }).subscribe({
      next: (comment) => {
        this.submittingComment.set(false);
        this.comments.update((comments) => [...comments, comment]);
        this.newCommentContent.set('');
        this.updateCommentCount(post, 1);
      },
      error: (error: unknown) => {
        this.submittingComment.set(false);
        this.postService.getErrorMessage(error);
      },
    });
  }

  startEdit(comment: Comment): void {
    this.editingCommentId.set(comment.id);
    this.editContent.set(comment.content);
  }

  cancelEdit(): void {
    this.editingCommentId.set(null);
    this.editContent.set('');
  }

  saveEdit(comment: Comment): void {
    const post = this.post();
    const content = this.editContent().trim();

    if (!post || !content || this.savingCommentId()) {
      return;
    }

    this.savingCommentId.set(comment.id);

    this.postApi.updateComment(post.id, comment.id, { content }).subscribe({
      next: (updated) => {
        this.savingCommentId.set(null);
        this.editingCommentId.set(null);
        this.comments.update((comments) =>
          comments.map((entry) => (entry.id === updated.id ? updated : entry)),
        );
      },
      error: (error: unknown) => {
        this.savingCommentId.set(null);
        this.postService.getErrorMessage(error);
      },
    });
  }

  requestDelete(comment: Comment): void {
    const post = this.post();

    if (!post) {
      return;
    }

    this.pendingCommentDeleteId = comment.id;

    this.confirmDialog().open({
      title: 'Delete comment',
      message: 'This will permanently delete your comment.',
      confirmLabel: 'Delete',
      danger: true,
    });
  }

  confirmDeleteComment(): void {
    const post = this.post();
    const commentId = this.pendingCommentDeleteId;

    if (!post || !commentId || this.deletingCommentId()) {
      return;
    }

    this.pendingCommentDeleteId = null;
    this.deletingCommentId.set(commentId);

    this.postApi.deleteComment(post.id, commentId).subscribe({
      next: () => {
        this.deletingCommentId.set(null);
        this.comments.update((comments) => comments.filter((entry) => entry.id !== commentId));
        this.updateCommentCount(post, -1);
      },
      error: (error: unknown) => {
        this.deletingCommentId.set(null);
        this.postService.getErrorMessage(error);
      },
    });
  }

  isOwnedComment(comment: Comment): boolean {
    const currentUserId = this.currentUserId();

    return !!currentUserId && comment.authorId === currentUserId;
  }

  /** Keeps the visible post counter in sync with the comment list. */
  private updateCommentCount(post: Post, delta: number): void {
    const nextCount = Math.max(0, post.commentsCount + delta);

    if (this.post()?.id === post.id) {
      this.post.update((current) => (current ? { ...current, commentsCount: nextCount } : current));
    }

    this.postService.changeCommentCount(post.id, delta, post.commentsCount);
  }
}
