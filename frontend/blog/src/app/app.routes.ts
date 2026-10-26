import { Routes } from '@angular/router';
import { AppLayout } from './layout/app-layout/app-layout';
import { AuthLayout } from './features/auth/layout/auth-layout';
import { authGuard, guestGuard } from './features/auth/guards/auth.guard';
import { Feed } from './features/feed/pages/feed';
import { Explore } from './features/explore/pages/explore';
import { Notifications } from './features/notifications/pages/notifications';
import { Block } from './features/block/pages/block';
import { NotFound } from './features/error/pages/not-found/not-found';

export const routes: Routes = [
  // Authentication pages — guests only
  {
    path: 'auth',
    // canActivate: [guestGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login',
      },
      {
        path: '',
        component: AuthLayout,
        children: [
          {
            path: 'login',
            title: 'Log in · 01Blog',
            loadComponent: () =>
              import('./features/auth/pages/login/login').then((m) => m.Login),
          },
          {
            path: 'register',
            title: 'Sign up · 01Blog',
            loadComponent: () =>
              import('./features/auth/pages/register/register').then(
                (m) => m.Register
              ),
          },
        ],
      },
    ],
  },

  // Main application — authenticated users only
  {
    path: '',
    component: AppLayout,
    // canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: Feed,
      },
      {
        path: 'explore',
        component: Explore,
      },
      {
        path: 'notifications',
        component: Notifications,
      },
      {
        path: 'block/me',
        component: Block,
      },
      {
        path: 'block/:username',
        component: Block,
      },
      // Anything else falls back to the 404 error page.
      {
        path: '**',
        component: NotFound,
      },
    ],
  },
];
