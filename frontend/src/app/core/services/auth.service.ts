import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
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

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly accessTokenStorageKey = 'gi_access_token';
  private readonly refreshTokenStorageKey = 'gi_refresh_token';

  readonly accessToken = signal<string | null>(localStorage.getItem(this.accessTokenStorageKey));
  readonly isAuthenticated = computed(() => !!this.accessToken());

  async login(identifier: string, password: string): Promise<void> {
    const response = await firstValueFrom(
      this.http.post<ApiRetour<LoginResponse>>(`${environment.apiBaseUrl}/api/public/auth/login`, {
        username: identifier,
        password
      })
    );

    this.storeTokens(response.data.accessToken, response.data.refreshToken);
  }

  async register(payload: {
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    password: string;
  }): Promise<void> {
    await firstValueFrom(
      this.http.post<ApiRetour<unknown>>(`${environment.apiBaseUrl}/api/public/auth/register`, payload)
    );
  }

  async getValidAccessToken(): Promise<string | null> {
    const token = this.accessToken();
    if (!token) return null;
    if (!this.isJwtExpired(token)) return token;
    return this.refreshToken();
  }

  async logout(): Promise<void> {
    localStorage.removeItem(this.accessTokenStorageKey);
    localStorage.removeItem(this.refreshTokenStorageKey);
    this.accessToken.set(null);
  }

  private async refreshToken(): Promise<string | null> {
    const refreshToken = localStorage.getItem(this.refreshTokenStorageKey);
    if (!refreshToken) {
      await this.logout();
      return null;
    }
    try {
      const response = await firstValueFrom(
        this.http.post<ApiRetour<LoginResponse>>(
          `${environment.apiBaseUrl}/api/public/auth/login/refresh`,
          { refreshToken }
        )
      );
      this.storeTokens(response.data.accessToken, response.data.refreshToken);
      return response.data.accessToken;
    } catch {
      await this.logout();
      return null;
    }
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
}
