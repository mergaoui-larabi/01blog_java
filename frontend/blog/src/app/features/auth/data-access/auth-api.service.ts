import { Injectable } from '@angular/core';
import { Observable, delay, of, throwError } from 'rxjs';

export interface LoginRequest {
  email: string;
  password: string;
  rememberMe: boolean;
}

export interface AuthUser {
  id: number;
  email: string;
  username: string;
  role: 'USER' | 'ADMIN';
}

@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly users = [
    {
      id: 1,
      email: 'larbi@example.com',
      password: 'password123',
      username: 'larbi',
      role: 'USER' as const,
    },
    {
      id: 2,
      email: 'admin@example.com',
      password: 'admin123',
      username: 'admin',
      role: 'ADMIN' as const,
    },
  ];

  login(request: LoginRequest): Observable<AuthUser> {
    const user = this.users.find(
      (candidate) =>
        candidate.email === request.email &&
        candidate.password === request.password,
    );

    if (!user) {
      return throwError(
        () => new Error('Invalid email or password.'),
      ).pipe(delay(800));
    }

    const { password: _, ...authUser } = user;

    return of(authUser).pipe(delay(800));
  }
}