import { Component, output, viewChild } from '@angular/core';
import { CreatePostDialog } from '../create-post-dialog/create-post-dialog';
import { Post } from '../../data-access/post.api.service';

@Component({
  selector: 'app-create-post-button',
  imports: [CreatePostDialog],
  templateUrl: './create-post-button.html',
  styleUrl: './create-post-button.scss',
})
export class CreatePostButton {
  private readonly createPostDialog = viewChild.required(CreatePostDialog);
  readonly published = output<Post | null>();

  openCreatePost(): void {
    this.createPostDialog().open();
  }

  onPostPublished(post: Post | null): void {
    this.published.emit(post);
  }
}
