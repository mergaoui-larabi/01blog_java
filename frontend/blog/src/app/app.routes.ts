import { Routes } from '@angular/router';
import { AppLayout } from './layout/app-layout/app-layout';
import { AuthLayout } from './features/auth/layout/auth-layout';
import { authGuard, guestGuard } from './features/auth/guards/auth.guard';

export const routes: Routes = [
  // Authentication pages — guests only
  {
    path: 'auth',
    canActivate: [guestGuard],
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
            loadComponent: () => import('./features/auth/pages/login/login').then((m) => m.Login),
          },
          {
            path: 'register',
            title: 'Sign up · 01Blog',
            loadComponent: () =>
              import('./features/auth/pages/register/register').then((m) => m.Register),
          },
        ],
      },
    ],
  },

  // Main application — authenticated users only
  {
    path: '',
    component: AppLayout,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        title: 'Feed · 01Blog',
        loadComponent: () => import('./features/feed/pages/feed/feed').then((m) => m.Feed),
      },
      {
        path: 'explore',
        title: 'Explore · 01Blog',
        loadComponent: () =>
          import('./features/explore/pages/explore/explore').then((m) => m.Explore),
      },
      {
        path: 'notifications',
        title: 'Notifications · 01Blog',
        loadComponent: () =>
          import('./features/notifications/pages/notifications/notifications').then(
            (m) => m.Notifications,
          ),
      },
      {
        path: 'block/me',
        title: 'My Block · 01Blog',
        loadComponent: () => import('./features/block/pages/block/block').then((m) => m.Block),
      },
      {
        path: 'block/:username',
        title: 'Block · 01Blog',
        loadComponent: () => import('./features/block/pages/block/block').then((m) => m.Block),
      },
      {
        path: '**',
        redirectTo: '',
      },
    ],
  },
];
