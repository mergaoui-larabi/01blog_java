import { Component, inject, OnDestroy, signal } from '@angular/core';

import { email, form, FormField, FormRoot, min, minLength, required } from '@angular/forms/signals';

import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthService } from '../../state/auth.service';

type Gender = 'MALE' | 'FEMALE';

const MAX_AVATAR_BYTES = 2 * 1024 * 1024;
const ACCEPTED_AVATAR_TYPES = ['image/png', 'image/jpeg', 'image/webp', 'image/gif'];

interface RegisterData {
  email: string;
  username: string;
  password: string;
  age: number | null;
  gender: Gender | '';
}

@Component({
  selector: 'app-register',
  imports: [FormField, FormRoot, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register implements OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  /** Staged avatar: previewed locally, sent to the media service later. */
  protected readonly avatarFile = signal<File | null>(null);
  protected readonly avatarPreviewUrl = signal<string | null>(null);
  protected readonly avatarError = signal('');

  protected readonly avatarAcceptedTypes = ACCEPTED_AVATAR_TYPES.join(',');

  ngOnDestroy(): void {
    this.revokeAvatarPreview();
  }

  protected onAvatarSelected(event: Event): void {
    const inputEl = event.target as HTMLInputElement;
    const file = inputEl.files?.[0];

    this.avatarError.set('');

    if (!file) {
      return;
    }

    if (!ACCEPTED_AVATAR_TYPES.includes(file.type)) {
      this.avatarError.set('Please choose a PNG, JPEG, GIF or WebP image.');
      inputEl.value = '';
      return;
    }

    if (file.size > MAX_AVATAR_BYTES) {
      this.avatarError.set('That image is larger than the 2 MB limit.');
      inputEl.value = '';
      return;
    }

    this.revokeAvatarPreview();
    this.avatarFile.set(file);
    this.avatarPreviewUrl.set(URL.createObjectURL(file));
  }

  protected removeAvatar(): void {
    this.revokeAvatarPreview();
    this.avatarFile.set(null);
    this.avatarError.set('');
  }

  private revokeAvatarPreview(): void {
    const url = this.avatarPreviewUrl();
    if (url) {
      URL.revokeObjectURL(url);
      this.avatarPreviewUrl.set(null);
    }
  }

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
          const file = this.avatarFile();
          this.authService.register(
            {
              email: data.email,
              username: data.username,
              password: data.password,
              age: data.age!,
              gender: data.gender as Gender,
            },
            file,
            this.route.snapshot.queryParamMap.get('returnUrl'),
          );
        },
      },
    },
  );
}
