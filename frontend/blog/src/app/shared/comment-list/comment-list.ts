import { Component, input, output, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Comment } from '../../core/models/comment.model';
import { CommentService } from '../../core/services/comment.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-comment-list',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './comment-list.html',
  styleUrl: './comment-list.css',
})
export class CommentList {
  readonly postId = input.required<number>();
  readonly comments = input<Comment[]>([]);
  readonly commentCreated = output<Comment>();

  private readonly commentService = inject(CommentService);
  readonly auth = inject(AuthService);

  commentControl = new FormControl('', [Validators.required, Validators.minLength(1)]);
  isSubmitting = false;

  submitComment(): void {
    if (this.commentControl.invalid || this.isSubmitting) return;

    this.isSubmitting = true;
    this.commentService.createComment(this.postId(), { content: this.commentControl.value! }).subscribe({
      next: (comment) => {
        this.commentCreated.emit(comment);
        this.commentControl.reset();
        this.isSubmitting = false;
      },
      error: () => {
        this.isSubmitting = false;
      },
    });
  }

  deleteComment(commentId: number): void {
    this.commentService.deleteComment(this.postId(), commentId).subscribe({
      next: () => {
        // Trigger parent to reload comments
        this.commentCreated.emit({} as Comment);
      },
    });
  }

  canDelete(comment: Comment): boolean {
    const user = this.auth.currentUser();
    return user?.id === comment.author.id || user?.role === 'ADMIN';
  }
}
