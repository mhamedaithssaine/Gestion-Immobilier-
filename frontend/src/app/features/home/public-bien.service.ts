import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiRetour } from '../../core/http/api-types';
import { BienResponse } from '../admin/models/admin-api.types';
import { SpringPage } from '../locataire/services/locataire-espace.service';

@Injectable({ providedIn: 'root' })
export class PublicBienService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/public/biens`;

  list(filters: { q?: string; ville?: string; page?: number; size?: number }): Observable<SpringPage<BienResponse>> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 4));
    if (filters.q?.trim()) params = params.set('q', filters.q.trim());
    if (filters.ville?.trim()) params = params.set('ville', filters.ville.trim());
    return this.http
      .get<ApiRetour<SpringPage<BienResponse>>>(this.base, { params })
      .pipe(map((r) => r.data));
  }
}

