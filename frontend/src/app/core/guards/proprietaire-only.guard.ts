import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Réservé au rôle propriétaire. */
export const proprietaireOnlyGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }
  if (auth.hasRole('ROLE_PROPRIETAIRE')) {
    return true;
  }
  return router.parseUrl('/login');
};

