import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  age: number;
  gender: 'MALE' | 'FEMALE';
}

export interface AuthUser {
  id: number;
  email: string;
  username: string;
  role: 'USER' | 'ADMIN';
  age: number;
  gender: 'MALE' | 'FEMALE';
}

@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/v1/auth';

  login(request: LoginRequest): Observable<AuthUser> {
    return this.http.post<AuthUser>(
      `${this.apiUrl}/login`,
      request,
    );
  }

  register(request: RegisterRequest): Observable<AuthUser> {
    return this.http.post<AuthUser>(
      `${this.apiUrl}/register`,
      request,
    );
  }
}