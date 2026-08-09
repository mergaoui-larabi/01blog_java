import { Component, inject } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { FormControl, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  loginForm = new FormGroup({
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)]),
  });

  error = '';
  isSubmitting = false;

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.isSubmitting = true;
    this.error = '';

    this.auth.login({
      username: this.loginForm.value.username!,
      password: this.loginForm.value.password!,
    }).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Invalid username or password. Please try again.';
        this.isSubmitting = false;
      },
    });
  }
}
