import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Réservé aux comptes **agent** (fiche agence, etc.). */
export const agentOnlyGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }
  if (auth.hasRole('ROLE_AGENT')) {
    return true;
  }
  return router.parseUrl('/login');
};
