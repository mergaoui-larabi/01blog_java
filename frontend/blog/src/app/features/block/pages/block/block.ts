import { Component, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { ProfilesService } from '../../../profiles/data-access/profiles.service';
import { PostsService } from '../../../posts/data-access/posts.service';
import { Post } from '../../../posts/models/post.model';
import { AuthService } from '../../../auth/state/auth.service';
import { PostCard } from '../../../posts/components/post-card/post-card';
import { Comments } from '../../../posts/components/comments/comments';
import { PostComposer } from '../../../posts/components/post-composer/post-composer';
import { ReportDialog } from '../../../report/components/report-dialog/report-dialog';
import { ReportTarget } from '../../../report/models/report.model';
import { Avatar } from '../../../../shared/ui/avatar/avatar';
import { Dialog } from '../../../../shared/ui/dialog/dialog';

/**
 * A user's public "Block". Renders the current user's profile (with post
 * management) or another user's profile (with subscribe/report actions)
 * depending on the route parameter.
 */
@Component({
  selector: 'app-block',
  templateUrl: './block.html',
  imports: [RouterLink, Avatar, PostCard, Comments, PostComposer, ReportDialog, Dialog],
})
export class Block {
  private readonly route = inject(ActivatedRoute);
  private readonly profilesService = inject(ProfilesService);
  private readonly postsService = inject(PostsService);
  private readonly authService = inject(AuthService);

  /** Route param: 'me' for the current user's Block, else a username. */
  readonly usernameParam = toSignal(this.route.paramMap.pipe(map((params) => params.get('username') ?? 'me')), {
    initialValue: 'me',
  });

  protected readonly expandedPostId = signal<string | null>(null);
  protected readonly editingPost = signal<Post | null>(null);
  protected readonly composerVisible = signal(false);
  protected readonly reportTarget = signal<ReportTarget | null>(null);
  protected readonly deleteCandidate = signal<Post | null>(null);
  protected readonly editProfileOpen = signal(false);
  protected readonly editName = signal('');
  protected readonly editBio = signal('');
  protected readonly profileSaved = signal(false);

  protected readonly username = computed(() => {
    const param = this.usernameParam();
    if (param === 'me') {
      return this.authService.user()?.username ?? '';
    }
    return param;
  });

  protected readonly isOwnBlock = computed(() => {
    const current = this.authService.user()?.username;
    return current !== undefined && current === this.username();
  });

  protected readonly notFound = computed(() => this.profilesService.profileByUsername(this.username()) === undefined);

  protected readonly profile = computed(() => this.profilesService.profileByUsername(this.username()));

  protected readonly posts = computed(() => this.postsService.postsByUsername(this.username()));

  protected readonly postCount = computed(() => this.posts().length);

  protected readonly isFollowing = computed(() => this.profilesService.isFollowing(this.username()));

  protected toggleFollow(): void {
    this.profilesService.toggleFollow(this.username());
  }

  protected openReportUser(): void {
    this.reportTarget.set({ type: 'USER', id: this.username(), label: this.username() });
  }

  protected openReportPost(post: Post): void {
    this.reportTarget.set({
      type: 'POST',
      id: post.id,
      label: post.content.slice(0, 60).replace(/\s+/g, ' ').trim() + (post.content.length > 60 ? '…' : ''),
    });
  }

  protected toggleLike(post: Post): void {
    this.postsService.toggleLike(post.id);
  }

  protected toggleComments(post: Post): void {
    this.expandedPostId.update((current) => (current === post.id ? null : post.id));
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

  protected openEditProfile(): void {
    const profile = this.profile();
    this.editName.set(profile?.displayName ?? '');
    this.editBio.set(profile?.bio ?? '');
    this.profileSaved.set(false);
    this.editProfileOpen.set(true);
  }

  protected saveProfile(): void {
    this.profilesService.updateMyProfile({
      displayName: this.editName().trim(),
      bio: this.editBio().trim(),
    });
    this.editProfileOpen.set(false);
    this.profileSaved.set(true);
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
