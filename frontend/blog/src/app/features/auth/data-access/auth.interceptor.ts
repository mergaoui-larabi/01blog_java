import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { environment } from '../../../../environments/environments';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenStorage = inject(TokenStorageService);
  const token = tokenStorage.token();

  // If there is no token or the request already specifies an Authorization header, pass through
  if (!token || request.headers.has('Authorization')) {
    return next(request);
  }
  const isAuthEndpoint =
    request.url.startsWith(environment.postLoginEndPoint) ||
    request.url.startsWith(environment.postRegisterEndPoint);
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
        // Clear the stored token when a protected endpoint rejects with 401
        tokenStorage.clearToken();
      }
      return throwError(() => error);
    }),
  );
};
