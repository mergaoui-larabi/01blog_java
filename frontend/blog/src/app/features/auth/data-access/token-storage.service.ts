import { inject, PLATFORM_ID, Service, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export const ACCESS_TOKEN_KEY = '01blog.accessToken';

@Service()
export class TokenStorageService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private readonly _token = signal<string | null>(this.readInitialToken());

  readonly token = this._token.asReadonly();

  /**
   * Stores only the access token in localStorage and updates the reactive token signal.
   *
   * The user session lives in memory and is populated by `AuthService.getCurrentUser()`
   * through the backend. Storing only the token in localStorage (and not the full session)
   * keeps the token easy to read from the current origin and avoids cookie constraints.
   */
  setToken(token: string): void {
    this._token.set(token);

    if (!this.isBrowser) {
      return;
    }

    try {
      localStorage.setItem(ACCESS_TOKEN_KEY, token);
    } catch {
      // Ignore quota or privacy-blocked writes
    }
  }

  /**
   * Clears the access token from localStorage and resets the reactive signal.
   */
  clearToken(): void {
    this._token.set(null);

    if (!this.isBrowser) {
      return;
    }

    try {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
    } catch {
      // Ignore
    }
  }

  /**
   * Retrieves the stored access token from localStorage.
   */
  getToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }

    try {
      return localStorage.getItem(ACCESS_TOKEN_KEY) ?? null;
    } catch {
      return null;
    }
  }

  private readInitialToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }

    try {
      return localStorage.getItem(ACCESS_TOKEN_KEY) ?? null;
    } catch {
      return null;
    }
  }
}
