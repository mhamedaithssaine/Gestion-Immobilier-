import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { UtilisateurResponse } from '../models/admin-api.types';

export interface Page<T> {
  content: T[];
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
}

@Injectable({ providedIn: 'root' })
export class AdminUserService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/admin`;

  listUsers(sync = true): Observable<UtilisateurResponse[]> {
    return this.http
      .get<ApiRetour<UtilisateurResponse[]>>(`${this.base}/users`, {
        params: { sync: String(sync) }
      })
      .pipe(map((r) => r.data));
  }

  getUsersPaged(sync = true, page = 0, size = 10): Observable<Page<UtilisateurResponse>> {
    return this.http
      .get<ApiRetour<Page<UtilisateurResponse>>>(`${this.base}/users/paged`, {
        params: {
          sync: String(sync),
          page: String(page),
          size: String(size)
        }
      })
      .pipe(map((r) => r.data));
  }

  createUser(payload: {
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    password: string;
    roles: string[];
  }): Observable<UtilisateurResponse> {
    return this.http
      .post<ApiRetour<UtilisateurResponse>>(`${this.base}/users`, payload)
      .pipe(map((r) => r.data));
  }

  assignRoles(id: string, roles: string[]): Observable<UtilisateurResponse> {
    return this.http
      .put<ApiRetour<UtilisateurResponse>>(`${this.base}/users/${id}/roles`, { roles })
      .pipe(map((r) => r.data));
  }

  setEnabled(id: string, enabled: boolean): Observable<UtilisateurResponse> {
    return this.http
      .put<ApiRetour<UtilisateurResponse>>(`${this.base}/users/${id}/enabled`, { enabled })
      .pipe(map((r) => r.data));
  }

  updateUser(
    id: string,
    payload: { username: string; email: string; firstName: string; lastName: string }
  ): Observable<UtilisateurResponse> {
    return this.http
      .put<ApiRetour<UtilisateurResponse>>(`${this.base}/users/${id}`, payload)
      .pipe(map((r) => r.data));
  }

  deleteUser(id: string): Observable<void> {
    return this.http
      .delete<ApiRetour<null>>(`${this.base}/users/${id}`)
      .pipe(map(() => undefined));
  }
}
