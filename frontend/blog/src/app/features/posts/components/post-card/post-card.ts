import { Component, computed, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, NgOptimizedImage } from '@angular/common';
import { Post } from '../../models/post.model';
import { AuthService } from '../../../auth/state/auth.service';
import { Avatar } from '../../../../shared/ui/avatar/avatar';
import { MarkdownService } from '../../../../shared/markdown/markdown.service';

/**
 * Presentational post card. Receives a typed `Post` and delegates every
 * mutation through outputs so the parent (feed, Block page) owns the state.
 *
 * The card is intentionally *not* wrapped in a single clickable element:
 * author links, media, text selection and action buttons stay independent.
 */
@Component({
  selector: 'app-post-card',
  templateUrl: './post-card.html',
  imports: [RouterLink, DatePipe, NgOptimizedImage, Avatar],
})
export class PostCard {
  private readonly markdownService = inject(MarkdownService);
  private readonly authService = inject(AuthService);

  readonly post = input.required<Post>();

  readonly like = output<void>();
  readonly toggleComments = output<void>();
  readonly report = output<void>();
  readonly edit = output<void>();
  readonly deleteRequested = output<void>();

  /** Whether the comments panel is expanded (controls aria-expanded). */
  readonly commentsOpen = input(false);

  protected readonly isOwner = computed(
    () => this.authService.user()?.username === this.post().author.username,
  );

  protected readonly contentHtml = computed(() => this.markdownService.render(this.post().content));

  protected readonly likeLabel = computed(() =>
    this.post().likedByMe
      ? `Remove like (${this.post().likeCount} likes)`
      : `Like this post (${this.post().likeCount} likes)`,
  );
}
