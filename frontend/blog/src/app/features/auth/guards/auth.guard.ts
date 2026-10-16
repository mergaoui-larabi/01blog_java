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
        // Replace with a real error page if you have one.
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

      if (status === 'error') {
        // Avoid treating a server outage as proof that the token is invalid.
        return router.parseUrl('/');
      }

      return true;
    }),
    catchError(() => of(router.parseUrl('/'))),
  );
};
