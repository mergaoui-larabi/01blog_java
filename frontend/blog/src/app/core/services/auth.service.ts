import { Injectable, inject, signal, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { User, LoginRequest, RegisterRequest, AuthResponse } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly apiUrl = '/api/auth';

  readonly currentUser = signal<User | null>(null);
  readonly isAuthenticated = signal(false);
  readonly isAdmin = signal(false);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.loadUser();
    }
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((res) => this.handleAuthResponse(res)),
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
      tap((res) => this.handleAuthResponse(res)),
    );
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  logout(): void {
    if (this.isBrowser()) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
    this.isAdmin.set(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    if (!this.isBrowser()) return null;
    return localStorage.getItem('token');
  }

  private handleAuthResponse(res: AuthResponse): void {
    if (this.isBrowser()) {
      localStorage.setItem('token', res.token);
      localStorage.setItem('user', JSON.stringify(res.user));
    }
    this.currentUser.set(res.user);
    this.isAuthenticated.set(true);
    this.isAdmin.set(res.user.role === 'ADMIN');
  }

  private loadUser(): void {
    if (!this.isBrowser()) return;
    const token = localStorage.getItem('token');
    const userJson = localStorage.getItem('user');
    if (token && userJson) {
      try {
        const user = JSON.parse(userJson) as User;
        this.currentUser.set(user);
        this.isAuthenticated.set(true);
        this.isAdmin.set(user.role === 'ADMIN');
      } catch {
        this.logout();
      }
    }
  }
}
