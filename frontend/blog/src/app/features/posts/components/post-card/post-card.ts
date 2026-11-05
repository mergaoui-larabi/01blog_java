import { Component, computed, effect, inject, input, output, signal, viewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Post } from '../../../posts/data-access/post.api.service';
import { PostService } from '../../state/post.service';
import { PostAuthorService } from '../../state/post-author.service';
import { MediaUrlService } from '../../../../shared/data-access/media-url.service';
import { Avatar } from '../../../../shared/ui/avatar/avatar';
import { ReportButton } from '../../../../shared/ui/buttons/report/report-button';
import { TimeAgoPipe } from '../../../../shared/pipes/time-ago.pipe';
import { EditPostDialog } from '../edit-post-dialog/edit-post-dialog';
import { ConfirmDialog } from '../../../../shared/ui/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-post-card',
  imports: [DatePipe, RouterLink, Avatar, TimeAgoPipe, EditPostDialog, ConfirmDialog, ReportButton],
  templateUrl: './post-card.html',
  styleUrl: './post-card.scss',
})
export class PostCard {
  readonly post = input.required<Post>();
  readonly clicked = output<Post>();

  private readonly postService = inject(PostService);
  private readonly authorService = inject(PostAuthorService);
  readonly mediaUrls = inject(MediaUrlService);

  readonly editDialog = viewChild(EditPostDialog);
  private readonly confirmDialog = viewChild.required(ConfirmDialog);

  readonly menuOpen = signal(false);
  private readonly maxVisibleMedia = 4;

  readonly author = computed(() => this.authorService.authorOf(this.post().authorId));

  readonly visibleMediaIds = computed(() => this.post().mediaIds.slice(0, this.maxVisibleMedia));
  readonly hiddenMediaCount = computed(() =>
    Math.max(0, this.post().mediaIds.length - this.maxVisibleMedia),
  );

  readonly likePending = computed(() => !!this.postService.pendingLikes()[this.post().id]);

  readonly previewContent = computed(() => {
    const content = this.post().content.trim();
    const maxLength = 240;

    if (content.length <= maxLength) {
      return content;
    }

    return `${content.slice(0, maxLength).trimEnd()}…`;
  });

  readonly isEdited = computed(() => {
    const post = this.post();

    return new Date(post.updatedAt).getTime() > new Date(post.createdAt).getTime();
  });

  constructor() {
    effect((onCleanup) => {
      const authorId = this.post().authorId;
      this.authorService.load(authorId);

      const mediaIds = this.post().mediaIds;
      for (const mediaId of mediaIds) {
        this.mediaUrls.ensure(mediaId);
      }
    });
  }

  openPost(): void {
    this.clicked.emit(this.post());
  }

  toggleLike(event: MouseEvent): void {
    event.stopPropagation();
    this.postService.toggleLike(this.post());
  }

  toggleMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  editPost(event: MouseEvent): void {
    event.stopPropagation();
    this.closeMenu();
    this.editDialog()?.open(this.post());
  }

  deletePost(event: MouseEvent): void {
    event.stopPropagation();
    this.closeMenu();
    this.confirmDialog().open({
      title: 'Delete post',
      message: 'This will permanently delete your post. This action cannot be undone.',
      confirmLabel: 'Delete',
      danger: true,
    });
  }

  onConfirmDelete(): void {
    this.postService.deletePost(this.post().id);
  }
}
