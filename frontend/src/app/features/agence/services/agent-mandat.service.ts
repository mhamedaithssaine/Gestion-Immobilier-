import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { MandatResponse, StatutMandat } from '../../admin/models/admin-api.types';

@Injectable({ providedIn: 'root' })
export class AgentMandatService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/agent/mandats`;

  listMandats(filters?: { statut?: StatutMandat | 'ALL' }): Observable<MandatResponse[]> {
    let params = new HttpParams();
    if (filters?.statut && filters.statut !== 'ALL') {
      params = params.set('statut', filters.statut);
    }
    return this.http
      .get<ApiRetour<MandatResponse[]>>(this.base, { params })
      .pipe(map((r) => r.data ?? []));
  }

  getMandat(id: string): Observable<MandatResponse> {
    return this.http
      .get<ApiRetour<MandatResponse>>(`${this.base}/${id}`)
      .pipe(map((r) => r.data as MandatResponse));
  }

  demanderResiliation(id: string): Observable<MandatResponse> {
    return this.http
      .patch<ApiRetour<MandatResponse>>(`${this.base}/${id}/demande-resiliation`, {})
      .pipe(map((r) => r.data as MandatResponse));
  }

  downloadDocument(id: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.base}/${id}/document`, {
      responseType: 'blob',
      observe: 'response'
    });
  }
}
