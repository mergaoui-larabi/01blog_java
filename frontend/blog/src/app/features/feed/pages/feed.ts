import { Component, inject, viewChild } from '@angular/core';
import { CreatePostButton } from '../../posts/components/create-post-button/create-post-button';
import { PostCard } from '../../posts/components/post-card/post-card';
import { FeedService } from '../state/feed.service';
import { Post } from '../data-access/feed.api.service';
import { CreatePostDialog } from '../../posts/components/create-post-dialog/create-post-dialog';
import { PostViewDialog } from '../../posts/components/post-view-dialog/post-view-dialog';

@Component({
  selector: 'app-feed',
  imports: [CreatePostButton, PostCard, CreatePostDialog, PostViewDialog],
  templateUrl: './feed.html',
})
export class Feed {
  readonly feedService = inject(FeedService);
  private readonly postViewDialog = viewChild.required(PostViewDialog);

  ngOnInit(): void {
    this.feedService.loadFeed();
  }

  openPost(post: Post): void {
    this.postViewDialog().open(post);
  }

  onPostPublished(post: Post | null) {
    if (post) this.feedService.addPostToFeed(post);
  }
}
