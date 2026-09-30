import { Component, inject, signal } from '@angular/core';

import {
  email,
  form,
  FormField,
  FormRoot,
  min,
  minLength,
  required,
} from '@angular/forms/signals';

import { RouterLink } from '@angular/router';

import { AuthService } from '../../state/auth.service';

type Gender = 'MALE' | 'FEMALE';

interface RegisterData {
  email: string;
  username: string;
  password: string;
  age: number | null;
  gender: Gender | '';
}

@Component({
  selector: 'app-register',
  imports: [
    FormField,
    FormRoot,
    RouterLink,
  ],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private readonly authService = inject(AuthService);

  protected readonly registerModel = signal<RegisterData>({
    email: '',
    username: '',
    password: '',
    age: null,
    gender: '',
  });

  protected readonly registerForm = form(
    this.registerModel,
    (path) => {
      required(path.email, {
        message: 'Email is required.',
      });

      email(path.email, {
        message: 'Please enter a valid email address.',
      });

      required(path.username, {
        message: 'Username is required.',
      });

      minLength(path.username, 3, {
        message: 'Username must be at least 3 characters.',
      });

      required(path.password, {
        message: 'Password is required.',
      });

      minLength(path.password, 8, {
        message: 'Password must be at least 8 characters.',
      });

      required(path.age, {
        message: 'Age is required.',
      });

      min(path.age, 13, {
        message: 'You must be at least 13 years old.',
      });

      required(path.gender, {
        message: 'Please select your gender.',
      });
    },
    {
      submission: {
        action: async (form) => {
          const data = form().value();

          this.authService.register({
            email: data.email,
            username: data.username,
            password: data.password,
            age: data.age!,
            gender: data.gender as Gender,
          });
        },
      },
    },
  );
}