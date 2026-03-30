import { Routes } from '@angular/router';
import { adminOnlyGuard } from './core/guards/admin-only.guard';
import { agentOnlyGuard } from './core/guards/agent-only.guard';
import { proprietaireOnlyGuard } from './core/guards/proprietaire-only.guard';
import { locataireOnlyGuard } from './core/guards/locataire-only.guard';

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
      },
      {
        path: 'register-agence',
        loadComponent: () =>
          import('./features/auth/register-agence/register-agence.component').then(
            (m) => m.RegisterAgenceComponent
          )
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
    canActivate: [adminOnlyGuard],
    loadComponent: () =>
      import('./features/admin/layout/admin-shell.component').then(
        (m) => m.AdminShellComponent
      ),
    children: [
      {
        path: 'vue-ensemble',
        loadComponent: () =>
          import('./features/admin/dashboard/admin-overview.component').then(
            (m) => m.AdminOverviewComponent
          )
      },
      {
        path: 'agences',
        children: [
          {
            path: 'list',
            loadComponent: () =>
              import('./features/admin/agences/admin-agences.component').then(
                (m) => m.AdminAgencesComponent
              )
          },
          {
            path: 'agents',
            loadComponent: () =>
              import('./features/admin/agences/admin-agents.component').then(
                (m) => m.AdminAgentsComponent
              )
          },
          {
            path: 'demandes',
            loadComponent: () =>
              import('./features/admin/agences/admin-agence-modifications.component').then(
                (m) => m.AdminAgenceModificationsComponent
              )
          },
          { path: '', redirectTo: 'list', pathMatch: 'full' }
        ]
      },
      {
        path: 'mandats',
        loadComponent: () =>
          import('./features/admin/mandats/admin-mandats.component').then(
            (m) => m.AdminMandatsComponent
          )
      },
      {
        path: 'utilisateurs',
        children: [
          {
            path: 'locataire',
            loadComponent: () =>
              import('./features/admin/users/admin-users.component').then(
                (m) => m.AdminUsersComponent
              )
          },
          {
            path: 'proprietaire',
            loadComponent: () =>
              import('./features/admin/users/admin-users.component').then(
                (m) => m.AdminUsersComponent
              )
          },
          { path: '', redirectTo: 'locataire', pathMatch: 'full' }
        ]
      },
      { path: '', redirectTo: 'vue-ensemble', pathMatch: 'full' }
    ]
  },
  {
    path: 'proprietaire',
    canActivate: [proprietaireOnlyGuard],
    loadComponent: () =>
      import('./features/proprietaire/layout/proprietaire-shell.component').then(
        (m) => m.ProprietaireShellComponent
      )
    ,
    children: [
      {
        path: 'tableau-de-bord',
        loadComponent: () =>
          import('./features/proprietaire/proprietaire-dashboard.component').then(
            (m) => m.ProprietaireDashboardComponent
          )
      },
      {
        path: 'biens',
        loadComponent: () =>
          import('./features/proprietaire/pages/biens/proprietaire-biens.component').then(
            (m) => m.ProprietaireBiensComponent
          )
      },
      {
        path: 'contrats',
        loadComponent: () =>
          import('./features/proprietaire/pages/contrats/proprietaire-contrats.component').then(
            (m) => m.ProprietaireContratsComponent
          )
      },
      {
        path: 'mandats',
        loadComponent: () =>
          import('./features/proprietaire/pages/mandats/proprietaire-mandats.component').then(
            (m) => m.ProprietaireMandatsComponent
          )
      },
      { path: '', redirectTo: 'tableau-de-bord', pathMatch: 'full' }
    ]
  },
  {
    path: 'agence',
    canActivate: [agentOnlyGuard],
    loadComponent: () =>
      import('./features/agence/layout/agence-shell.component').then((m) => m.AgenceShellComponent),
    children: [
      {
        path: 'tableau-de-bord',
        loadComponent: () =>
          import('./features/agence/pages/dashboard/agence-dashboard.component').then(
            (m) => m.AgenceDashboardComponent
          )
      },
      {
        path: 'mon-agence',
        loadComponent: () =>
          import('./features/agence/agence-espace.component').then((m) => m.AgenceEspaceComponent)
      },
      {
        path: 'biens',
        loadComponent: () =>
          import('./features/agence/pages/biens/agence-biens.component').then((m) => m.AgenceBiensComponent)
      },
      {
        path: 'mandats',
        loadComponent: () =>
          import('./features/agence/pages/mandats/agence-mandats.component').then(
            (m) => m.AgenceMandatsComponent
          )
      },
      {
        path: 'contrats',
        loadComponent: () =>
          import('./features/agence/pages/contrats/agent-contrats-page.component').then(
            (m) => m.AgentContratsPageComponent
          )
      },
      { path: '', redirectTo: 'tableau-de-bord', pathMatch: 'full' }
    ]
  },
  {
    path: 'locataire',
    canActivate: [locataireOnlyGuard],
    loadComponent: () =>
      import('./features/locataire/layout/locataire-shell.component').then((m) => m.LocataireShellComponent),
    children: [
      {
        path: 'biens',
        loadComponent: () =>
          import('./features/locataire/pages/biens/locataire-biens-page.component').then(
            (m) => m.LocataireBiensPageComponent
          )
      },
      {
        path: 'mes-demandes',
        loadComponent: () =>
          import('./features/locataire/pages/mes-demandes/locataire-demandes-page.component').then(
            (m) => m.LocataireDemandesPageComponent
          )
      },
      { path: '', redirectTo: 'biens', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];
