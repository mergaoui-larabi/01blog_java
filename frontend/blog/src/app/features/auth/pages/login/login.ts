import { Component, inject, signal } from '@angular/core';
import {
  email,
  form,
  FormField,
  FormRoot,
  minLength,
  required,
} from '@angular/forms/signals';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../state/auth.service';

interface LoginData {
  email: string;
  password: string;
}

@Component({
  selector: 'app-login',
  imports: [
    FormField,
    FormRoot,
    RouterLink
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  protected readonly loginModel = signal<LoginData>({
    email: '',
    password: ''
  });

  protected readonly loginForm = form(
    this.loginModel,
    (path) => {
      required(path.email, {
        message: 'Email is required.',
      });

      email(path.email, {
        message: 'Please enter a valid email address.',
      });

      required(path.password, {
        message: 'Password is required.',
      });

      minLength(path.password, 8, {
        message: 'Password must be at least 8 characters.',
      });
    },
    {
      submission: {
        action: async (form) => {
          const credentials = form().value();

          this.authService.login(
            {
              email: credentials.email,
              password: credentials.password,
            },
            this.route.snapshot.queryParamMap.get('returnUrl'),
          );
        },
      },
    },
  );
}