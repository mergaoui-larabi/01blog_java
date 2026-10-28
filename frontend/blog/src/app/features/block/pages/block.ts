import { Component, inject } from '@angular/core';
import { FeedService } from '../../feed/state/feed.service';
import { Post } from '../../posts/data-access/post.api.service';
import { PostCard } from '../../posts/components/post-card/post-card';
import { CreatePostButton } from '../../posts/components/create-post-button/create-post-button';
import { BlockService } from '../state/block.service';
import { CreatePostDialog } from '../../posts/components/create-post-dialog/create-post-dialog';

@Component({
  selector: 'app-block',
  imports: [CreatePostButton, PostCard, CreatePostDialog],
  templateUrl: './block.html',
})
export class Block {
  readonly blockService = inject(BlockService);

  ngOnInit(): void {
    this.blockService.loadFeed();
  }

  openPost(post: Post): void {
    // TODO: Open the post-detail dialog.
    console.log('Selected post:', post.id);
  }

  onPostPublished(post: Post | null) {
    if (post) this.blockService.addPostToFeed(post);
  }
}
