import { inject, PLATFORM_ID, Service, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export const ACCESS_TOKEN_KEY = '01blog.accessToken';

export interface CookieOptions {
  expires?: number | Date;
  path?: string;
  domain?: string;
  secure?: boolean;
  sameSite?: 'Lax' | 'Strict' | 'None';
}

@Service()
export class TokenStorageService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private readonly _token = signal<string | null>(this.readInitialToken());

  readonly token = this._token.asReadonly();

  /**
   * Stores the access token in a cookie and updates the reactive signal.
   *
   * @param token JWT access token
   * @param expiresInSeconds Optional lifetime in seconds (e.g. from login response)
   */
  setToken(token: string, expiresInSeconds?: number): void {
    this._token.set(token);

    if (!this.isBrowser) {
      return;
    }

    const options: CookieOptions = {
      path: '/',
      sameSite: 'Lax',
    };

    if (expiresInSeconds && expiresInSeconds > 0) {
      options.expires = new Date(Date.now() + expiresInSeconds * 1000);
    } else {
      // Default to 7 days
      options.expires = 7;
    }

    this.setCookie(ACCESS_TOKEN_KEY, token, options);

    // Clean up any legacy sessionStorage token if present
    this.cleanLegacyStorage();
  }

  /**
   * Clears the access token from cookies and resets the reactive signal.
   */
  clearToken(): void {
    this._token.set(null);

    if (!this.isBrowser) {
      return;
    }

    this.deleteCookie(ACCESS_TOKEN_KEY, '/');
    // Also clean fallback key if it was ever set
    this.deleteCookie('accessToken', '/');

    this.cleanLegacyStorage();
  }

  /**
   * Retrieves a cookie value by name.
   */
  getCookie(name: string): string | null {
    if (!this.isBrowser || typeof document === 'undefined') {
      return null;
    }

    const nameEQ = encodeURIComponent(name) + '=';
    const cookies = document.cookie ? document.cookie.split(';') : [];

    for (let cookie of cookies) {
      cookie = cookie.trim();
      if (cookie.indexOf(nameEQ) === 0) {
        return decodeURIComponent(cookie.substring(nameEQ.length));
      }
    }

    return null;
  }

  /**
   * Sets a cookie with specified options.
   */
  setCookie(name: string, value: string, options: CookieOptions = {}): void {
    if (!this.isBrowser || typeof document === 'undefined') {
      return;
    }

    let cookieStr = `${encodeURIComponent(name)}=${encodeURIComponent(value)}`;

    const path = options.path ?? '/';
    cookieStr += `; path=${path}`;

    if (options.expires) {
      if (typeof options.expires === 'number') {
        const date = new Date();
        date.setTime(date.getTime() + options.expires * 24 * 60 * 60 * 1000);
        cookieStr += `; expires=${date.toUTCString()}`;
      } else {
        cookieStr += `; expires=${options.expires.toUTCString()}`;
      }
    }

    if (options.domain) {
      cookieStr += `; domain=${options.domain}`;
    }

    const sameSite = options.sameSite ?? 'Lax';
    cookieStr += `; SameSite=${sameSite}`;

    const secure =
      options.secure ??
      (typeof location !== 'undefined' && location.protocol === 'https:');
    if (secure) {
      cookieStr += '; Secure';
    }

    document.cookie = cookieStr;
  }

  /**
   * Deletes a cookie by expiring it immediately.
   */
  deleteCookie(name: string, path: string = '/', domain?: string): void {
    if (!this.isBrowser || typeof document === 'undefined') {
      return;
    }

    let cookieStr = `${encodeURIComponent(name)}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; Max-Age=0; path=${path}`;
    if (domain) {
      cookieStr += `; domain=${domain}`;
    }
    document.cookie = cookieStr;
  }

  private readInitialToken(): string | null {
    const cookieToken =
      this.getCookie(ACCESS_TOKEN_KEY) ?? this.getCookie('accessToken');

    if (cookieToken) {
      return cookieToken;
    }

    // One-time fallback migration from sessionStorage if present
    if (this.isBrowser) {
      try {
        const sessionToken = sessionStorage.getItem(ACCESS_TOKEN_KEY);
        if (sessionToken) {
          this.setToken(sessionToken);
          sessionStorage.removeItem(ACCESS_TOKEN_KEY);
          return sessionToken;
        }
      } catch {
        // Ignore
      }
    }

    return null;
  }

  private cleanLegacyStorage(): void {
    try {
      sessionStorage.removeItem(ACCESS_TOKEN_KEY);
      sessionStorage.removeItem('01blog.session');
    } catch {
      // Ignore
    }
  }
}
