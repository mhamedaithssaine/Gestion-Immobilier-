import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Réservé au rôle locataire/client. */
export const locataireOnlyGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }
  if (auth.hasRole('ROLE_CLIENT')) {
    return true;
  }
  return router.parseUrl('/login');
};

