import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Accès réservé aux comptes **admin** ou **agent** (aligné sur `@PreAuthorize` du dashboard). */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }
  if (auth.hasAnyRole('ROLE_ADMIN', 'ROLE_AGENT')) {
    return true;
  }
  return router.parseUrl('/dashboard');
};
