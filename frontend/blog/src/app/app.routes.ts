import { Routes } from '@angular/router';
import { AppLayout } from './layout/app-layout/app-layout';
import { AuthLayout } from './features/auth/layout/auth-layout';

export const routes: Routes = [
    {
        path: '',
        component: AppLayout,
        children: []
    },

    {
        path: 'auth',
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
                        loadComponent: () =>
                            import('./features/auth/pages/login/login')
                                .then(m => m.Login),
                    },
                    {
                        path: 'register',
                        loadComponent: () =>
                            import('./features/auth/pages/register/register')
                                .then(m => m.Register),
                    },
                ],
            },
        ],
    },
];