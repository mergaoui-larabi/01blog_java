import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { PostService } from '../../../core/services/post.service';
import { Post } from '../../../core/models/post.model';

@Component({
  selector: 'app-post-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './post-form.html',
  styleUrl: './post-form.css',
})
export class PostForm implements OnInit {
  private readonly postService = inject(PostService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  isEditMode = false;
  postId?: number;
  isLoading = false;
  isSubmitting = false;
  error = '';
  selectedFile: File | null = null;
  mediaPreview: string | ArrayBuffer | null = null;

  postForm = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.maxLength(200)]),
    content: new FormControl('', [Validators.required, Validators.minLength(10)]),
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.postId = Number(id);
      this.loadPost(this.postId);
    }
  }

  private loadPost(id: number): void {
    this.isLoading = true;
    this.postService.getPost(id).subscribe({
      next: (post) => {
        this.postForm.patchValue({
          title: post.title,
          content: post.content,
        });
        if (post.mediaUrl) {
          this.mediaPreview = post.mediaUrl;
        }
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Post not found.';
        this.isLoading = false;
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];

      const reader = new FileReader();
      reader.onload = () => {
        this.mediaPreview = reader.result;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  removeMedia(): void {
    this.selectedFile = null;
    this.mediaPreview = null;
  }

  onSubmit(): void {
    if (this.postForm.invalid || this.isSubmitting) return;

    this.isSubmitting = true;
    this.error = '';

    const { title, content } = this.postForm.value;

    if (this.isEditMode && this.postId) {
      this.postService.updatePost(this.postId, {
        title: title!,
        content: content!,
        media: this.selectedFile ?? undefined,
      }).subscribe({
        next: () => {
          this.router.navigate(['/posts', this.postId]);
        },
        error: (err) => {
          this.error = err.error?.message || 'Failed to update post.';
          this.isSubmitting = false;
        },
      });
    } else {
      this.postService.createPost({
        title: title!,
        content: content!,
        media: this.selectedFile ?? undefined,
      }).subscribe({
        next: (post) => {
          this.router.navigate(['/posts', post.id]);
        },
        error: (err) => {
          this.error = err.error?.message || 'Failed to create post.';
          this.isSubmitting = false;
        },
      });
    }
  }
}
