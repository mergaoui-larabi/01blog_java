import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PostsService } from '../../../posts/data-access/posts.service';
import { Post } from '../../../posts/models/post.model';
import { PostCard } from '../../../posts/components/post-card/post-card';
import { Comments } from '../../../posts/components/comments/comments';
import { PostComposer } from '../../../posts/components/post-composer/post-composer';
import { ReportDialog } from '../../../report/components/report-dialog/report-dialog';
import { ReportTarget } from '../../../report/models/report.model';
import { Dialog } from '../../../../shared/ui/dialog/dialog';

/**
 * The authenticated landing page: posts from the users the student follows,
 * plus the composer. All mutations delegate to PostsService so the feed, the
 * Block page, and the post card stay consistent.
 */
@Component({
  selector: 'app-feed',
  templateUrl: './feed.html',
  imports: [RouterLink, PostCard, Comments, PostComposer, ReportDialog, Dialog],
})
export class Feed {
  private readonly postsService = inject(PostsService);

  /** Mock loading state: flipped once on init to demonstrate the skeleton. */
  protected readonly loading = signal(true);

  protected readonly posts = this.postsService.feedPosts;

  protected readonly expandedPostId = signal<string | null>(null);
  protected readonly editingPost = signal<Post | null>(null);
  protected readonly reportTarget = signal<ReportTarget | null>(null);
  protected readonly deleteCandidate = signal<Post | null>(null);
  protected readonly composerVisible = signal(false);

  protected readonly expandedCommentsOpen = computed(() => this.expandedPostId() !== null);

  constructor() {
    setTimeout(() => this.loading.set(false), 400);
  }

  protected toggleLike(post: Post): void {
    this.postsService.toggleLike(post.id);
  }

  protected toggleComments(post: Post): void {
    this.expandedPostId.update((current) => (current === post.id ? null : post.id));
  }

  protected openReport(post: Post): void {
    this.reportTarget.set({
      type: 'POST',
      id: post.id,
      label: post.content.slice(0, 60).replace(/\s+/g, ' ').trim() + (post.content.length > 60 ? '…' : ''),
    });
  }

  protected startEdit(post: Post): void {
    this.editingPost.set(post);
    this.composerVisible.set(true);
  }

  protected confirmDelete(): void {
    const post = this.deleteCandidate();
    if (post) {
      this.postsService.deletePost(post.id);
      if (this.expandedPostId() === post.id) {
        this.expandedPostId.set(null);
      }
    }
    this.deleteCandidate.set(null);
  }

  protected onComposerSubmitted(): void {
    this.composerVisible.set(false);
    this.editingPost.set(null);
  }

  protected excerpt(post: Post): string {
    const text = post.content.replace(/[#>*`_\[\]()!-]/g, '').replace(/\s+/g, ' ').trim();
    return text.length > 80 ? text.slice(0, 80) + '…' : text;
  }
}
