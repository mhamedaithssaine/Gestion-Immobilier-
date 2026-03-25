import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, defer, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiRetour } from '../http/api-types';

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  refreshExpiresIn: number;
  scope: string;
}

/** Rôles autorisés à l'inscription publique (alignés sur l'enum backend `Role`). */
export type PublicRegistrationRole = 'ROLE_CLIENT' | 'ROLE_PROPRIETAIRE';

export type RegisterPayload = {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  /** Un seul rôle : locataire = ROLE_CLIENT, propriétaire = ROLE_PROPRIETAIRE */
  roles: PublicRegistrationRole[];
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly accessTokenStorageKey = 'gi_access_token';
  private readonly refreshTokenStorageKey = 'gi_refresh_token';

  readonly accessToken = signal<string | null>(localStorage.getItem(this.accessTokenStorageKey));
  readonly isAuthenticated = computed(() => !!this.accessToken());

  /** Rôles JWT Keycloak normalisés avec préfixe `ROLE_`. */
  readonly roles = computed(() => {
    const token = this.accessToken();
    return token ? this.extractRolesFromToken(token) : [];
  });

  hasRole(role: string): boolean {
    const r = role.startsWith('ROLE_') ? role : 'ROLE_' + role.toUpperCase();
    return this.roles().includes(r);
  }

  hasAnyRole(...check: string[]): boolean {
    return check.some((c) => this.hasRole(c));
  }

  /**
   * Connexion : flux RxJS aligné sur HttpClient (annulation, opérateurs, etc.).
   */
  login(identifier: string, password: string): Observable<void> {
    return this.http
      .post<ApiRetour<LoginResponse>>(`${environment.apiBaseUrl}/api/public/auth/login`, {
        username: identifier,
        password
      })
      .pipe(
        tap((response) =>
          this.storeTokens(response.data.accessToken, response.data.refreshToken)
        ),
        map(() => undefined)
      );
  }

  register(payload: RegisterPayload): Observable<ApiRetour<unknown>> {
    return this.http.post<ApiRetour<unknown>>(
      `${environment.apiBaseUrl}/api/public/auth/register`,
      payload
    );
  }

  /**
   * Jeton valide pour l’intercepteur : Observable (compose avec switchMap).
   */
  getValidAccessToken$(): Observable<string | null> {
    return defer(() => {
      const token = this.accessToken();
      if (!token) {
        return of(null);
      }
      if (!this.isJwtExpired(token)) {
        return of(token);
      }
      return this.refreshAccessToken$();
    });
  }

  logout(): void {
    this.clearTokens();
  }

  private refreshAccessToken$(): Observable<string | null> {
    const refreshToken = localStorage.getItem(this.refreshTokenStorageKey);
    if (!refreshToken) {
      this.clearTokens();
      return of(null);
    }
    return this.http
      .post<ApiRetour<LoginResponse>>(
        `${environment.apiBaseUrl}/api/public/auth/login/refresh`,
        { refreshToken }
      )
      .pipe(
        tap((response) =>
          this.storeTokens(response.data.accessToken, response.data.refreshToken)
        ),
        map((response) => response.data.accessToken),
        catchError(() => {
          this.clearTokens();
          return of(null);
        })
      );
  }

  private clearTokens(): void {
    localStorage.removeItem(this.accessTokenStorageKey);
    localStorage.removeItem(this.refreshTokenStorageKey);
    this.accessToken.set(null);
  }

  private storeTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.accessTokenStorageKey, accessToken);
    localStorage.setItem(this.refreshTokenStorageKey, refreshToken);
    this.accessToken.set(accessToken);
  }

  private isJwtExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1])) as { exp?: number };
      if (!payload.exp) return true;
      return payload.exp <= Math.floor(Date.now() / 1000) + 10;
    } catch {
      return true;
    }
  }

  private extractRolesFromToken(token: string): string[] {
    try {
      const payload = JSON.parse(atob(token.split('.')[1])) as {
        realm_access?: { roles?: string[] };
      };
      const raw = payload.realm_access?.roles ?? [];
      return raw
        .map((x) => {
          const s = String(x).trim();
          if (!s) return '';
          return s.startsWith('ROLE_') ? s : 'ROLE_' + s.toUpperCase();
        })
        .filter(Boolean);
    } catch {
      return [];
    }
  }
}
