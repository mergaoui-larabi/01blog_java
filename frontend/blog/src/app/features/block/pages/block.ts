import { Component, inject, viewChild } from '@angular/core';
import { Post } from '../../posts/data-access/post.api.service';
import { PostCard } from '../../posts/components/post-card/post-card';
import { CreatePostButton } from '../../posts/components/create-post-button/create-post-button';
import { BlockService } from '../state/block.service';
import { CreatePostDialog } from '../../posts/components/create-post-dialog/create-post-dialog';
import { PostViewDialog } from '../../posts/components/post-view-dialog/post-view-dialog';
import { Avatar } from '../../../shared/ui/avatar/avatar';

@Component({
  selector: 'app-block',
  imports: [CreatePostButton, PostCard, CreatePostDialog, PostViewDialog, Avatar],
  templateUrl: './block.html',
})
export class Block {
  readonly blockService = inject(BlockService);
  private readonly postViewDialog = viewChild.required(PostViewDialog);

  ngOnInit(): void {
    this.blockService.loadFeed();
  }

  openPost(post: Post): void {
    this.postViewDialog().open(post);
  }

  onPostPublished(post: Post | null) {
    if (post) this.blockService.addPostToFeed(post);
  }
}
