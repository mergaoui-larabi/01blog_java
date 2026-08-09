import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Post } from '../../core/models/post.model';
import { PostService } from '../../core/services/post.service';
import { AuthService } from '../../core/services/auth.service';
import { ReportService } from '../../core/services/report.service';
import { PostCard } from '../../shared/post-card/post-card';
import { ReportModal } from '../../shared/report-modal/report-modal';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [PostCard, ReportModal],
  templateUrl: './feed.html',
  styleUrl: './feed.css',
})
export class Feed implements OnInit {
  private readonly postService = inject(PostService);
  private readonly reportService = inject(ReportService);
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  posts: Post[] = [];
  currentPage = 0;
  isLoading = false;
  hasMore = true;
  error = '';

  // Report modal state
  reportModalVisible = false;
  reportUserId = 0;

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts(): void {
    if (this.isLoading || !this.hasMore) return;

    this.isLoading = true;
    this.postService.getFeed(this.currentPage).subscribe({
      next: (posts) => {
        if (posts.length < 10) {
          this.hasMore = false;
        }
        this.posts = [...this.posts, ...posts];
        this.currentPage++;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Failed to load posts. Please try again.';
        this.isLoading = false;
      },
    });
  }

  onLikeToggled(post: Post): void {
    if (post.isLiked) {
      this.postService.unlikePost(post.id).subscribe({
        next: () => {
          post.isLiked = false;
          post.likeCount--;
        },
      });
    } else {
      this.postService.likePost(post.id).subscribe({
        next: () => {
          post.isLiked = true;
          post.likeCount++;
        },
      });
    }
  }

  onDeleteRequested(post: Post): void {
    if (!confirm('Are you sure you want to delete this post?')) return;

    this.postService.deletePost(post.id).subscribe({
      next: () => {
        this.posts = this.posts.filter((p) => p.id !== post.id);
      },
    });
  }

  onReportRequested(userId: number): void {
    this.reportUserId = userId;
    this.reportModalVisible = true;
  }

  onReportSubmitted(data: { reportedUserId: number; reason: string }): void {
    this.reportService.createReport(data).subscribe({
      next: () => {
        this.reportModalVisible = false;
        alert('Report submitted successfully. An administrator will review it.');
      },
      error: () => {
        alert('Failed to submit report. Please try again.');
      },
    });
  }

  onReportModalClose(): void {
    this.reportModalVisible = false;
  }

  onCreatePost(): void {
    this.router.navigate(['/posts/new']);
  }
}
