import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Timestamp } from 'rxjs';
import { environment } from '../../../../environments/environments';

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

export interface UpdateAvatarIdRequest {
  ownerId: string;
  avatarMediaId: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
}

export interface AuthUser {
  id: string;
  email: string;
  username: string;
  role: 'USER' | 'ADMIN';
  age: number;
  gender: 'MALE' | 'FEMALE';
  avatarMediaId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface MediaResponse {
  id: string;
  ownerId: string;
  purpose: string;
  status: string;
  originalFilename: string;
  detectedContentType: string;
  sizeBytes: number;
  width: number | null;
  height: number | null;
  durationMs: number | null;
  createdAt: string;
  updatedAt: string;
}

@Service()
export class AuthApiService {
  private readonly http = inject(HttpClient);

  private readonly env = environment;

  //get
  getCurrentUser(): Observable<AuthUser> {
    return this.http.get<AuthUser>(this.env.getMeEndpoint);
  }

  //post
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.env.postLoginEndPoint, request);
  }

  register(request: RegisterRequest): Observable<AuthUser> {
    return this.http.post<AuthUser>(this.env.postRegisterEndPoint, request);
  }

  uploadAvatar(file: File): Observable<MediaResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<MediaResponse>(this.env.postAvatarEndpoint, formData);
  }

  //patch
  updateAvatarId(request: UpdateAvatarIdRequest): Observable<AuthUser> {
    return this.http.patch<AuthUser>(this.env.patchAvatarIdEndpoint, request);
  }
}
