import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { TokenStorageService } from './token-storage.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenStorage = inject(TokenStorageService);
  const token = tokenStorage.token();

  // If there is no token or the request already specifies an Authorization header, pass through
  if (!token || request.headers.has('Authorization')) {
    return next(request);
  }

  // Do not attach token to public authentication endpoints (signin / signup)
  // to avoid 401 errors from expired/stale tokens on public routes.
  const isAuthEndpoint =
    request.url.includes('/auth/signin') ||
    request.url.includes('/users/signup');

  if (isAuthEndpoint) {
    return next(request);
  }

  const authReq = request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authReq).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        // Clear token from cookie and state when 401 Unauthorized occurs on a protected endpoint
        tokenStorage.clearToken();
      }
      return throwError(() => error);
    }),
  );
};
