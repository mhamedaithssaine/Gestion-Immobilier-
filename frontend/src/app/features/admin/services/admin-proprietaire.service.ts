import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { ProprietaireResponse } from '../models/admin-api.types';

@Injectable({ providedIn: 'root' })
export class AdminProprietaireService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/admin/proprietaires`;

  list(): Observable<ProprietaireResponse[]> {
    return this.http
      .get<ApiRetour<ProprietaireResponse[]>>(this.base)
      .pipe(map((r) => r.data));
  }

  create(payload: {
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    password: string;
    rib?: string;
    adresseContact?: string;
  }): Observable<ProprietaireResponse> {
    return this.http
      .post<ApiRetour<ProprietaireResponse>>(this.base, payload)
      .pipe(map((r) => r.data));
  }

  update(
    id: string,
    payload: { rib?: string | null; adresseContact?: string | null }
  ): Observable<ProprietaireResponse> {
    return this.http
      .put<ApiRetour<ProprietaireResponse>>(`${this.base}/${id}`, payload)
      .pipe(map((r) => r.data));
  }

  delete(id: string): Observable<void> {
    return this.http
      .delete<ApiRetour<null>>(`${this.base}/${id}`)
      .pipe(map(() => undefined));
  }
}
