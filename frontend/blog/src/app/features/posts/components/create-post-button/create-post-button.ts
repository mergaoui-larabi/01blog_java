import { Component, viewChild } from '@angular/core';
import { CreatePostDialog } from '../create-post-dialog/create-post-dialog';

@Component({
  selector: 'app-create-post-button',
  imports: [CreatePostDialog],
  templateUrl: './create-post-button.html',
})
export class CreatePostButton {
  private readonly createPostDialog = viewChild.required(CreatePostDialog);

  openCreatePost(): void {
    this.createPostDialog().open();
  }
}
