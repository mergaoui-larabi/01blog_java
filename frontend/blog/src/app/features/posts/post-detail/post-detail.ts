import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { Post } from '../../../core/models/post.model';
import { Comment } from '../../../core/models/comment.model';
import { PostService } from '../../../core/services/post.service';
import { CommentService } from '../../../core/services/comment.service';
import { AuthService } from '../../../core/services/auth.service';
import { ReportService } from '../../../core/services/report.service';
import { CommentList } from '../../../shared/comment-list/comment-list';
import { ReportModal } from '../../../shared/report-modal/report-modal';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [DatePipe, RouterLink, CommentList, ReportModal],
  templateUrl: './post-detail.html',
  styleUrl: './post-detail.css',
})
export class PostDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly postService = inject(PostService);
  private readonly commentService = inject(CommentService);
  private readonly reportService = inject(ReportService);
  readonly auth = inject(AuthService);

  post?: Post;
  comments: Comment[] = [];
  isLoading = true;
  error = '';

  // Report modal state
  reportModalVisible = false;
  reportUserId = 0;

  ngOnInit(): void {
    const postId = Number(this.route.snapshot.paramMap.get('id'));
    if (!postId) {
      this.router.navigate(['/']);
      return;
    }
    this.loadPost(postId);
    this.loadComments(postId);
  }

  private loadPost(id: number): void {
    this.isLoading = true;
    this.postService.getPost(id).subscribe({
      next: (post) => {
        this.post = post;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Post not found.';
        this.isLoading = false;
      },
    });
  }

  private loadComments(postId: number): void {
    this.commentService.getComments(postId).subscribe({
      next: (comments) => {
        this.comments = comments;
      },
    });
  }

  onLikeToggled(): void {
    if (!this.post) return;
    if (this.post.isLiked) {
      this.postService.unlikePost(this.post.id).subscribe({
        next: () => {
          this.post!.isLiked = false;
          this.post!.likeCount--;
        },
      });
    } else {
      this.postService.likePost(this.post.id).subscribe({
        next: () => {
          this.post!.isLiked = true;
          this.post!.likeCount++;
        },
      });
    }
  }

  onDeletePost(): void {
    if (!this.post) return;
    if (!confirm('Are you sure you want to delete this post?')) return;

    this.postService.deletePost(this.post.id).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
    });
  }

  onCommentCreated(): void {
    if (this.post) {
      this.loadComments(this.post.id);
    }
  }

  onReportRequested(): void {
    if (!this.post) return;
    this.reportUserId = this.post.author.id;
    this.reportModalVisible = true;
  }

  onReportSubmitted(data: { reportedUserId: number; reason: string }): void {
    this.reportService.createReport(data).subscribe({
      next: () => {
        this.reportModalVisible = false;
        alert('Report submitted successfully.');
      },
      error: () => {
        alert('Failed to submit report.');
      },
    });
  }

  onReportModalClose(): void {
    this.reportModalVisible = false;
  }

  canModify(): boolean {
    const user = this.auth.currentUser();
    return user?.id === this.post?.author.id || user?.role === 'ADMIN';
  }
}
