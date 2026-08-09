import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register').then((m) => m.Register),
  },
  {
    path: '',
    loadComponent: () => import('./features/feed/feed').then((m) => m.Feed),
    canActivate: [authGuard],
  },
  {
    path: 'posts/new',
    loadComponent: () =>
      import('./features/posts/post-form/post-form').then((m) => m.PostForm),
    canActivate: [authGuard],
  },
  {
    path: 'posts/:id',
    loadComponent: () =>
      import('./features/posts/post-detail/post-detail').then((m) => m.PostDetail),
  },
  {
    path: 'posts/:id/edit',
    loadComponent: () =>
      import('./features/posts/post-form/post-form').then((m) => m.PostForm),
    canActivate: [authGuard],
  },
  {
    path: 'profile/:username',
    loadComponent: () =>
      import('./features/profile/profile').then((m) => m.Profile),
  },
  {
    path: 'notifications',
    loadComponent: () =>
      import('./features/notifications/notifications-list').then((m) => m.NotificationsList),
    canActivate: [authGuard],
  },
  {
    path: 'admin',
    loadComponent: () =>
      import('./features/admin/admin-dashboard').then((m) => m.AdminDashboard),
    canActivate: [authGuard, adminGuard],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
