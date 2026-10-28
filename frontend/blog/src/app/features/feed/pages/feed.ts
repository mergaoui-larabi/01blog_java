import { Component, inject } from '@angular/core';
import { CreatePostButton } from '../../posts/components/create-post-button/create-post-button';
import { PostCard } from '../../posts/components/post-card/post-card';
import { FeedService } from '../state/feed.service';
import { Post } from '../data-access/feed.api.service';
import { CreatePostDialog } from '../../posts/components/create-post-dialog/create-post-dialog';

@Component({
  selector: 'app-feed',
  imports: [CreatePostButton, PostCard, CreatePostDialog],
  templateUrl: './feed.html',
})
export class Feed {
  readonly feedService = inject(FeedService);

  ngOnInit(): void {
    this.feedService.loadFeed();
  }

  openPost(post: Post): void {
    // TODO: Open the post-detail dialog.
    console.log('Selected post:', post.id);
  }

  onPostPublished(post: Post | null) {
    if (post) this.feedService.addPostToFeed(post);
  }
}
