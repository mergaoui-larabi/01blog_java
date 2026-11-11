import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { environment } from '../../../../environments/environments';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  const token = tokenStorage.token();

  // Skip requests without a token or with an existing Authorization header.
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
      if (error instanceof HttpErrorResponse) {
        // Authentication failure
        if (error.status === 401) {
          tokenStorage.clearToken();
          router.navigate(['/auth/login']);
        }

        // Authorization failure
        if (error.status === 403) {
          console.log('Authorization error:', error);
          const errorCode = error.error?.code;

          switch (errorCode) {
            case 'ACCOUNT_BANNED':
              tokenStorage.clearToken();
              router.navigate(['/auth/login']);
              break;

            case 'ACCOUNT_DELETED':
              tokenStorage.clearToken();
              router.navigate(['/auth/login']);
              break;

            default:
              break;
          }
        }
      }

      return throwError(() => error);
    }),
  );
};
