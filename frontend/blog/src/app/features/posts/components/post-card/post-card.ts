import { Component, computed, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Post } from '../../../feed/data-access/feed.api.service';

@Component({
  selector: 'app-post-card',
  imports: [DatePipe],
  templateUrl: './post-card.html',
})
export class PostCard {
  readonly post = input.required<Post>();
  readonly clicked = output<Post>();

  readonly previewContent = computed(() => {
    const content = this.post().content.trim();
    const maxLength = 240;

    if (content.length <= maxLength) {
      return content;
    }

    return `${content.slice(0, maxLength).trimEnd()}…`;
  });

  readonly isEdited = computed(() => {
    const post = this.post();

    return new Date(post.updatedAt).getTime() > new Date(post.createdAt).getTime();
  });

  openPost(): void {
    console.log('Selected post:', this.post());
    this.clicked.emit(this.post());
  }
}
