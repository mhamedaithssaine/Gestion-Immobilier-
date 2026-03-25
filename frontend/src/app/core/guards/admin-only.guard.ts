import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Réservé au **ROLE_ADMIN** (agences, utilisateurs Keycloak). */
export const adminOnlyGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.hasRole('ROLE_ADMIN')) {
    return true;
  }
  return router.parseUrl('/admin/vue-ensemble');
};
