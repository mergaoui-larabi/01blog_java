import { Injectable, inject, signal } from '@angular/core';

import {
    AuthApiService,
    AuthUser,
    LoginRequest,
} from '../data-access/auth-api.service';

@Injectable({
    providedIn: 'root',
})

export class AuthService {
    private readonly authApiService = inject(AuthApiService);

    private readonly currentUser = signal<AuthUser | null>(null);

    readonly user = this.currentUser.asReadonly();

    login(request: LoginRequest) {
        return this.authApiService.login(request);
    }

    setAuthenticatedUser(user: AuthUser): void {
        this.currentUser.set(user);
    }

    logout(): void {
        this.currentUser.set(null);
    }

    isAuthenticated(): boolean {
        return this.currentUser() !== null;
    }
}