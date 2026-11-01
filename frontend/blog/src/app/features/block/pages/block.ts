import { Component, inject, signal, viewChild } from '@angular/core';
import { Post } from '../../posts/data-access/post.api.service';
import { PostCard } from '../../posts/components/post-card/post-card';
import { CreatePostButton } from '../../posts/components/create-post-button/create-post-button';
import { BlockService } from '../state/block.service';
import { PostViewDialog } from '../../posts/components/post-view-dialog/post-view-dialog';
import { UserProfile } from '../../explore/components/user-profile/user-profile';
import { InfiniteScroll } from '../../../shared/directives/infinite-scroll';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { distinctUntilChanged, map, switchMap, tap } from 'rxjs';
import { UserSearch } from '../../explore/data-access/explore.api.service';
import { ExploreService } from '../../explore/state/explore.service';

@Component({
  selector: 'app-block',
  imports: [CreatePostButton, PostCard, PostViewDialog, UserProfile, InfiniteScroll],
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
        tap(() => {
          this.user.set(null);
          this.blockService.reset();
        }),
        switchMap((username) => {
          if (!username) {
            throw new Error('Username route parameter is missing');
          }

          return this.blockService.getUserByUsername(username).pipe(
            tap((user) => {
              this.user.set(user);

              if (username === 'me' || user.sameUser) {
                this.blockService.loadFeed();
              }
            }),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe();
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
