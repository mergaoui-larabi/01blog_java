import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { AuthService } from '../state/auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.validateSession().pipe(
    map((status) => {
      if (status === 'authenticated') {
        return true;
      }

      if (status === 'error') {
        return router.createUrlTree(['/auth/login'], {
          queryParams: {
            returnUrl: state.url,
            sessionError: 'true',
          },
        });
      }

      return router.createUrlTree(['/auth/login'], {
        queryParams: { returnUrl: state.url },
      });
    }),
    catchError(() => of(router.createUrlTree(['/auth/login']))),
  );
};

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return authService.validateSession().pipe(
    map((status) => {
      if (status === 'authenticated') {
        return router.parseUrl('/');
      }

      return true;
    }),
    catchError(() => of(true)),
  );
};

/**
 * Functional guard for role-based routes. Requests the user session
 * from the backend via getCurrentUser() and checks that the user matches the expected role.
 */
export const roleGuard = (expectedRole: 'USER' | 'ADMIN'): CanActivateFn => {
  return (_route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    return authService.getCurrentUser().pipe(
      map((user) => {
        if (user.role === expectedRole) {
          return true;
        }
        return router.parseUrl('/');
      }),
      catchError(() =>
        of(
          router.createUrlTree(['/auth/login'], {
            queryParams: { returnUrl: state.url },
          }),
        ),
      ),
    );
  };
};
