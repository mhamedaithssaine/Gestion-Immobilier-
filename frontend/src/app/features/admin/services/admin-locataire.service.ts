import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { LocataireResponse, StatutDossier } from '../models/admin-api.types';

@Injectable({ providedIn: 'root' })
export class AdminLocataireService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/admin/locataires`;

  list(): Observable<LocataireResponse[]> {
    return this.http
      .get<ApiRetour<LocataireResponse[]>>(this.base)
      .pipe(map((r) => r.data));
  }

  create(payload: {
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    password: string;
    budgetMax?: number | null;
    statutDossier?: StatutDossier | null;
  }): Observable<LocataireResponse> {
    return this.http
      .post<ApiRetour<LocataireResponse>>(this.base, payload)
      .pipe(map((r) => r.data));
  }

  update(
    id: string,
    payload: { budgetMax?: number | null; statutDossier?: StatutDossier | null }
  ): Observable<LocataireResponse> {
    return this.http
      .put<ApiRetour<LocataireResponse>>(`${this.base}/${id}`, payload)
      .pipe(map((r) => r.data));
  }

  delete(id: string): Observable<void> {
    return this.http
      .delete<ApiRetour<null>>(`${this.base}/${id}`)
      .pipe(map(() => undefined));
  }
}
