import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PostsService } from '../../data-access/posts.service';
import { AuthService } from '../../../auth/state/auth.service';
import { Avatar } from '../../../../shared/ui/avatar/avatar';

const MAX_LENGTH = 1000;

/**
 * Comments panel for a single post. Owns its own input state; the comment
 * store lives in PostsService so counts stay consistent everywhere.
 *
 * Rendered inside a disclosure controlled by the parent's post card
 * (`aria-controls` points at this region's id).
 */
@Component({
  selector: 'app-comments',
  templateUrl: './comments.html',
  imports: [RouterLink, DatePipe, Avatar],
})
export class Comments {
  private readonly postsService = inject(PostsService);
  private readonly authService = inject(AuthService);

  readonly postId = input.required<string>();
  /** Region id so the card's aria-controls/aria-expanded pair resolves. */
  readonly regionId = input.required<string>();

  protected readonly comments = computed(() => this.postsService.commentsForPost(this.postId()));

  protected readonly draft = signal('');
  protected readonly error = signal('');
  protected readonly submitting = signal(false);
  protected readonly success = signal(false);

  protected readonly maxLength = MAX_LENGTH;

  protected get currentUsername(): string {
    return this.authService.user()?.username ?? '';
  }

  protected submit(): void {
    const content = this.draft().trim();

    this.error.set('');
    this.success.set(false);

    if (!content) {
      this.error.set('Comment cannot be empty.');
      return;
    }

    if (content.length > MAX_LENGTH) {
      this.error.set(`Comments are limited to ${MAX_LENGTH} characters.`);
      return;
    }

    const username = this.currentUsername;
    if (!username) {
      this.error.set('You need to be signed in to comment.');
      return;
    }

    this.submitting.set(true);

    // Mock persistence: local state only, no network involved.
    setTimeout(() => {
      this.postsService.addComment(this.postId(), content, username);
      this.draft.set('');
      this.submitting.set(false);
      this.success.set(true);
    }, 250);
  }
}
