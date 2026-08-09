import { Component, input, output, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Post } from '../../core/models/post.model';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './post-card.html',
  styleUrl: './post-card.css',
})
export class PostCard {
  readonly post = input.required<Post>();
  readonly showFullContent = input(false);

  readonly likeToggled = output<Post>();
  readonly deleteRequested = output<Post>();
  readonly reportRequested = output<number>();

  readonly auth = inject(AuthService);

  onToggleLike(): void {
    this.likeToggled.emit(this.post());
  }

  onDelete(): void {
    this.deleteRequested.emit(this.post());
  }

  onReport(): void {
    this.reportRequested.emit(this.post().author.id);
  }

  canModify(): boolean {
    const user = this.auth.currentUser();
    return user?.id === this.post().author.id || user?.role === 'ADMIN';
  }

  getContentPreview(): string {
    const content = this.post().content;
    if (this.showFullContent()) return content;
    return content.length > 200 ? content.substring(0, 200) + '...' : content;
  }

  getRelativeTime(date: string): string {
    const now = new Date();
    const postDate = new Date(date);
    const diffMs = now.getTime() - postDate.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));

    if (diffHours < 1) {
      const diffMins = Math.floor(diffMs / (1000 * 60));
      return diffMins <= 1 ? 'just now' : `${diffMins}m ago`;
    }
    if (diffHours < 24) return `${diffHours}h ago`;
    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 7) return `${diffDays}d ago`;
    return postDate.toLocaleDateString();
  }
}
