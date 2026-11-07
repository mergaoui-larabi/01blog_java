import { inject, PLATFORM_ID, Service, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export const ACCESS_TOKEN_KEY = '01blog.accessToken';

@Service()
export class TokenStorageService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private readonly _token = signal<string | null>(this.readInitialToken());

  readonly token = this._token.asReadonly();

  setToken(token: string): void {
    this._token.set(token);

    if (!this.isBrowser) {
      return;
    }

    try {
      localStorage.setItem(ACCESS_TOKEN_KEY, token);
    } catch {
      return;
    }
  }

  clearToken(): void {
    this._token.set(null);

    if (!this.isBrowser) {
      return;
    }

    try {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
    } catch {
      return;
    }
  }

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
