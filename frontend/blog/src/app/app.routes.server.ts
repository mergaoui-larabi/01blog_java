import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'login',
    renderMode: RenderMode.Server,
  },
  {
    path: 'register',
    renderMode: RenderMode.Server,
  },
  {
    path: '',
    renderMode: RenderMode.Server,
  },
  {
    path: 'posts/new',
    renderMode: RenderMode.Server,
  },
  {
    path: 'posts/:id',
    renderMode: RenderMode.Server,
  },
  {
    path: 'posts/:id/edit',
    renderMode: RenderMode.Server,
  },
  {
    path: 'profile/:username',
    renderMode: RenderMode.Server,
  },
  {
    path: 'notifications',
    renderMode: RenderMode.Server,
  },
  {
    path: 'admin',
    renderMode: RenderMode.Server,
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];
