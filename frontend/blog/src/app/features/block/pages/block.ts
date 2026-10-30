import { Component, inject, signal, viewChild } from '@angular/core';
import { Post } from '../../posts/data-access/post.api.service';
import { PostCard } from '../../posts/components/post-card/post-card';
import { CreatePostButton } from '../../posts/components/create-post-button/create-post-button';
import { BlockService } from '../state/block.service';
import { CreatePostDialog } from '../../posts/components/create-post-dialog/create-post-dialog';
import { PostViewDialog } from '../../posts/components/post-view-dialog/post-view-dialog';
import { Avatar } from '../../../shared/ui/avatar/avatar';
import { UserProfile } from '../../explore/components/user-profile/user-profile';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { distinctUntilChanged, map, switchMap, tap } from 'rxjs';
import { UserSearch } from '../../explore/data-access/explore.api.service';
import { ExploreService } from '../../explore/state/explore.service';

@Component({
  selector: 'app-block',
  imports: [CreatePostButton, PostCard, CreatePostDialog, PostViewDialog, Avatar, UserProfile],
  templateUrl: './block.html',
})
export class Block {
  private readonly route = inject(ActivatedRoute);
  readonly blockService = inject(BlockService);
  private readonly exploreService = inject(ExploreService);
  private readonly postViewDialog = viewChild.required(PostViewDialog);

  private readonly username = this.route.paramMap.pipe(
    map((params) => params.get('username')),
    distinctUntilChanged(),
  );

  readonly user = signal<UserSearch | null>(null);

  constructor() {
    this.username
      .pipe(
        // The component is reused when the :username param changes, so clear
        // any previously shown posts before fetching the new profile's data.
        tap(() => {
          this.user.set(null);
          this.blockService.reset();
          this.blockService.loadFeed();
        }),
        switchMap((username) => {
          if (!username) {
            throw new Error('Username route parameter is missing');
          }

          return this.blockService.getUserByUsername(username);
        }),
        takeUntilDestroyed(),
      )
      .subscribe((user) => this.user.set(user));
  }

  onFollowChange(event: { userId: string; followed: boolean }): void {
    const applyFollowState = (followed: boolean) => {
      this.user.update((user) =>
        user && user.id === event.userId ? { ...user, followedByCurrentUser: followed } : user,
      );
    };

    if (event.followed) this.exploreService.followUser(event.userId, applyFollowState);
    if (!event.followed) this.exploreService.unfollowUser(event.userId, applyFollowState);
  }

  openPost(post: Post): void {
    this.postViewDialog().open(post);
  }

  onPostPublished(post: Post | null) {
    if (post) this.blockService.addPostToFeed(post);
  }
}
