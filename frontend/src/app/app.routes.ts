import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { adminOnlyGuard } from './core/guards/admin-only.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/public-layout/public-layout.component').then(
        (m) => m.PublicLayoutComponent
      ),
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () =>
          import('./features/home/home.component').then((m) => m.HomeComponent)
      },
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/login/login.component').then((m) => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/register/register.component').then((m) => m.RegisterComponent)
      }
    ]
  },
  // {
  //   path: 'dashboard',
  //   canActivate: [authGuard],
  //   loadComponent: () =>
  //     import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent)
  // },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/admin/layout/admin-shell.component').then(
        (m) => m.AdminShellComponent
      ),
    children: [
      {
        path: 'vue-ensemble',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/admin/dashboard/admin-overview.component').then(
            (m) => m.AdminOverviewComponent
          )
      },
      {
        path: 'agences',
        canActivate: [adminOnlyGuard],
        loadComponent: () =>
          import('./features/admin/agences/admin-agences.component').then(
            (m) => m.AdminAgencesComponent
          )
      },
      {
        path: 'utilisateurs',
        canActivate: [adminOnlyGuard],
        loadComponent: () =>
          import('./features/admin/users/admin-users.component').then(
            (m) => m.AdminUsersComponent
          )
      },
      { path: '', redirectTo: 'vue-ensemble', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];
