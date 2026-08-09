import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Post } from '../../core/models/post.model';
import { User } from '../../core/models/user.model';
import { PostService } from '../../core/services/post.service';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { ReportService } from '../../core/services/report.service';
import { PostCard } from '../../shared/post-card/post-card';
import { ReportModal } from '../../shared/report-modal/report-modal';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [DatePipe, RouterLink, PostCard, ReportModal],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly postService = inject(PostService);
  private readonly userService = inject(UserService);
  private readonly reportService = inject(ReportService);
  readonly auth = inject(AuthService);

  profileUser?: User;
  posts: Post[] = [];
  isLoading = true;
  error = '';
  isSubscribed = false;

  // Report modal state
  reportModalVisible = false;

  ngOnInit(): void {
    const username = this.route.snapshot.paramMap.get('username');
    if (!username) {
      this.router.navigate(['/']);
      return;
    }
    this.loadProfile(username);
  }

  private loadProfile(username: string): void {
    this.isLoading = true;
    this.userService.getUser(username).subscribe({
      next: (user) => {
        this.profileUser = user;
        this.isSubscribed = user.isSubscribed ?? false;
        this.isLoading = false;
        this.loadPosts(username);
      },
      error: () => {
        this.error = 'User not found.';
        this.isLoading = false;
      },
    });
  }

  private loadPosts(username: string): void {
    this.postService.getUserPosts(username).subscribe({
      next: (posts) => {
        this.posts = posts;
      },
    });
  }

  toggleSubscribe(): void {
    if (!this.profileUser) return;

    if (this.isSubscribed) {
      this.userService.unsubscribe(this.profileUser.username).subscribe({
        next: () => {
          this.isSubscribed = false;
          this.profileUser!.subscriberCount--;
        },
      });
    } else {
      this.userService.subscribe(this.profileUser.username).subscribe({
        next: () => {
          this.isSubscribed = true;
          this.profileUser!.subscriberCount++;
        },
      });
    }
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
        if (this.profileUser) {
          this.profileUser.postCount--;
        }
      },
    });
  }

  onReportRequested(): void {
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

  isOwnProfile(): boolean {
    return this.auth.currentUser()?.username === this.profileUser?.username;
  }
}
