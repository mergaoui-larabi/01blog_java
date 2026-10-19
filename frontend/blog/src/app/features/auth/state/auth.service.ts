import { HttpErrorResponse } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';

import {
  AuthApiService,
  AuthUser,
  LoginRequest,
  RegisterRequest,
} from '../data-access/auth.api.service';

const SESSION_STORAGE_KEY = '01blog.session';
const ACCESS_TOKEN_KEY = '01blog.accessToken';

export type AuthStatus = 'checking' | 'authenticated' | 'unauthenticated' | 'error';

@Service()
export class AuthService {
  private readonly authApi = inject(AuthApiService);
  private readonly router = inject(Router);

  private readonly currentUser = signal<AuthUser | null>(this.restoreSession());
  private readonly token = signal<string | null>(sessionStorage.getItem(ACCESS_TOKEN_KEY));

  readonly user = this.currentUser.asReadonly();
  readonly accessToken = this.token.asReadonly();

  readonly status = signal<AuthStatus>('unauthenticated');
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    this.validateSession().subscribe();
  }

  validateSession(): Observable<AuthStatus> {
    const token = this.token();

    if (!token) {
      this.status.set('unauthenticated');
      return of('unauthenticated');
    }

    this.status.set('checking');
    this.error.set(null);

    return this.authApi.getCurrentUser().pipe(
      tap((user) => {
        this.setAuthenticatedUser(user);
      }),

      map((): AuthStatus => 'authenticated'),

      catchError((err: unknown) => {
        if (err instanceof HttpErrorResponse && err.status === 401) {
          this.clearSession();
          this.error.set('Your session has expired. Please log in again.');
          return of('unauthenticated' as AuthStatus);
        }

        this.status.set('error');
        this.error.set('Unable to verify your session. Please try again.');

        return of('error' as AuthStatus);
      }),
    );
  }

  login(request: LoginRequest, returnUrl?: string | null): void {
    this.loading.set(true);
    this.error.set(null);
    this.status.set('checking');

    this.authApi
      .login(request)
      .pipe(
        tap((response) => {
          this.token.set(response.accessToken);
          sessionStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
        }),

        switchMap(() => this.authApi.getCurrentUser()),

        tap((user) => {
          this.setAuthenticatedUser(user);

          void this.router.navigateByUrl(this.safeReturnUrl(returnUrl));
        }),

        catchError((err: unknown) => {
          this.error.set(this.getErrorMessage(err));
          this.clearSession();

          return EMPTY;
        }),

        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  register(request: RegisterRequest, avatarFile: File | null, returnUrl?: string | null): void {
    this.loading.set(true);
    this.error.set(null);

    this.authApi
      .register(request)
      .pipe(
        // 1. Register the user, then log in.
        switchMap(() =>
          this.authApi.login({
            email: request.email,
            password: request.password,
          }),
        ),

        // 2. Store the JWT before calling protected endpoints.
        tap((loginResponse) => {
          this.token.set(loginResponse.accessToken);
          sessionStorage.setItem(ACCESS_TOKEN_KEY, loginResponse.accessToken);
        }),

        // 3. Upload the avatar only if one was selected.
        switchMap(() => {
          if (!avatarFile) {
            return of(null);
          }

          return this.authApi.uploadAvatar(avatarFile);
        }),

        // 4. Associate the uploaded media ID with the user.
        switchMap((uploadResponse) => {
          if (!uploadResponse) {
            return this.authApi.getCurrentUser();
          }

          return this.authApi.updateAvatarId({
            ownerId: uploadResponse.ownerId,
            avatarMediaId: uploadResponse.id,
          });
        }),

        // 5. Save the final user state.
        tap((user) => {
          this.setAuthenticatedUser(user);
        }),

        // 6. Navigate after the workflow completes.
        tap(() => {
          void this.router.navigateByUrl(this.safeReturnUrl(returnUrl));
        }),

        catchError((err: unknown) => {
          this.error.set(this.getErrorMessage(err));
          return EMPTY;
        }),

        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  setAuthenticatedUser(user: AuthUser): void {
    this.currentUser.set(user);
    this.status.set('authenticated');

    sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(user));
  }

  logout(): void {
    this.clearSession();
    this.error.set(null);

    void this.router.navigateByUrl('/auth/login');
  }

  isAuthenticated(): boolean {
    return this.status() === 'authenticated';
  }

  private clearSession(): void {
    this.currentUser.set(null);
    this.token.set(null);
    this.status.set('unauthenticated');

    sessionStorage.removeItem(SESSION_STORAGE_KEY);
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
  }

  private restoreSession(): AuthUser | null {
    const raw = sessionStorage.getItem(SESSION_STORAGE_KEY);

    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as AuthUser;
    } catch {
      sessionStorage.removeItem(SESSION_STORAGE_KEY);
      return null;
    }
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Cannot connect to the server.';
      }

      if (error.status === 401) {
        return 'Invalid credentials or expired session.';
      }

      if (error.status === 403) {
        return 'You do not have permission to perform this action.';
      }

      if (error.status === 409) {
        return 'An account with this email or username already exists.';
      }

      const message = error.error?.message;

      if (typeof message === 'string') {
        return message;
      }
    }

    return 'An unexpected error occurred. Please try again.';
  }

  private safeReturnUrl(returnUrl: string | null | undefined): string {
    if (returnUrl?.startsWith('/') && !returnUrl.startsWith('//') && !returnUrl.startsWith('/\\')) {
      return returnUrl;
    }

    return '/';
  }
}
