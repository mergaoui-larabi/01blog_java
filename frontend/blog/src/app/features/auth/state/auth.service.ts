import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';

import {
  AuthApiService,
  AuthUser,
  LoginRequest,
  RegisterRequest,
} from '../data-access/auth.api.service';
import { AlertService } from '../../../core/alerts/alert.service';
import { TokenStorageService } from '../data-access/token-storage.service';

export type AuthStatus = 'checking' | 'authenticated' | 'unauthenticated' | 'error';

@Service()
export class AuthService {
  private readonly authApi = inject(AuthApiService);
  private readonly router = inject(Router);
  private readonly alertService = inject(AlertService);
  private readonly tokenStorage = inject(TokenStorageService);

  // User session is stored in reactive memory only (not persisted in sessionStorage or cookies)
  private readonly currentUser = signal<AuthUser | null>(null);

  readonly user = this.currentUser.asReadonly();
  readonly role = computed(() => this.currentUser()?.role ?? null);
  readonly accessToken = this.tokenStorage.token;

  readonly status = signal<AuthStatus>('unauthenticated');
  readonly loading = signal(false);

  constructor() {
    // If an access token exists in cookies on startup, validate and fetch the current user
    if (this.tokenStorage.token()) {
      this.validateSession().subscribe();
    }
  }

  /**
   * Requests the current user session from the backend via HttpClient.
   * Updates in-memory user state and sets status to authenticated.
   */
  getCurrentUser(): Observable<AuthUser> {
    return this.authApi.getCurrentUser().pipe(
      tap((user) => {
        this.setAuthenticatedUser(user);
      }),
    );
  }

  /**
   * Validates the session token and populates the current user.
   */
  validateSession(): Observable<AuthStatus> {
    const token = this.tokenStorage.token();

    if (!token) {
      this.clearSession();
      return of('unauthenticated');
    }

    this.status.set('checking');

    return this.getCurrentUser().pipe(
      map((): AuthStatus => 'authenticated'),
      catchError((err: unknown) => {
        if (err instanceof HttpErrorResponse && err.status === 401) {
          this.clearSession();
          this.alertService.warning('Your session has expired. Please log in again.');
          return of('unauthenticated' as AuthStatus);
        }

        console.error('[Session] Validation error:', err);
        this.status.set('error');
        this.alertService.error('Unable to verify your session. Please try again.');

        return of('error' as AuthStatus);
      }),
    );
  }

  login(request: LoginRequest, returnUrl?: string | null): void {
    this.loading.set(true);
    this.status.set('checking');

    this.authApi
      .login(request)
      .pipe(
        // 1. Store only the accessToken in cookies
        tap((response) => {
          this.tokenStorage.setToken(response.accessToken, response.expiresIn);
        }),

        // 2. Request the session from the backend via getCurrentUser
        switchMap(() => this.getCurrentUser()),

        tap(() => {
          this.alertService.success('Logged in successfully.');
          void this.router.navigateByUrl(this.safeReturnUrl(returnUrl));
        }),

        catchError((err: unknown) => {
          this.getErrorMessage(err);
          this.clearSession();
          return EMPTY;
        }),

        finalize(() => {
          this.loading.set(false);
        }),
      )
      .subscribe();
  }

  register(request: RegisterRequest, avatarFile: File | null, returnUrl?: string | null): void {
    this.loading.set(true);

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

        // 2. Store only the accessToken in cookies before calling protected endpoints.
        tap((loginResponse) => {
          this.tokenStorage.setToken(loginResponse.accessToken, loginResponse.expiresIn);
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
            return this.getCurrentUser();
          }

          return this.authApi.updateAvatarId({
            ownerId: uploadResponse.ownerId,
            avatarMediaId: uploadResponse.id,
          });
        }),

        // 5. Save the final user state in memory.
        tap((user) => {
          this.setAuthenticatedUser(user);
        }),

        // 6. Navigate after the workflow completes.
        tap(() => {
          this.alertService.success('Account created successfully. Welcome!');
          void this.router.navigateByUrl(this.safeReturnUrl(returnUrl));
        }),

        catchError((err: unknown) => {
          this.getErrorMessage(err);
          return EMPTY;
        }),

        finalize(() => {
          this.loading.set(false);
        }),
      )
      .subscribe();
  }

  setAuthenticatedUser(user: AuthUser): void {
    this.currentUser.set(user);
    this.status.set('authenticated');
  }

  logout(): void {
    this.clearSession();
    void this.router.navigateByUrl('/auth/login');
    this.alertService.success('You have been logged out successfully.');
  }

  isAuthenticated(): boolean {
    return this.status() === 'authenticated';
  }

  hasRole(role: 'USER' | 'ADMIN'): boolean {
    return this.currentUser()?.role === role;
  }

  private clearSession(): void {
    this.currentUser.set(null);
    this.tokenStorage.clearToken();
    this.status.set('unauthenticated');
  }

  private getErrorMessage(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        this.alertService.error('Cannot connect to the server.');
        return;
      }
      const message = error.error?.message;
      return this.alertService.error(message);
    }
    this.alertService.error('An unexpected error occurred. Please try again.');
  }

  private safeReturnUrl(returnUrl: string | null | undefined): string {
    if (returnUrl?.startsWith('/') && !returnUrl.startsWith('//') && !returnUrl.startsWith('/\\')) {
      return returnUrl;
    }

    return '/';
  }
}
