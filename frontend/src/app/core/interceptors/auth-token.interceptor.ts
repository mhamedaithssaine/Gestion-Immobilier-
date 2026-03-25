import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/api/public/')) {
    return next(req);
  }

  const authService = inject(AuthService);
  return authService.getValidAccessToken$().pipe(
    switchMap((token) => {
      if (!token) return next(req);
      return next(
        req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        })
      );
    })
  );
};
