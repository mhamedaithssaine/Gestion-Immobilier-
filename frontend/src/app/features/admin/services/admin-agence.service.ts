import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { AgenceResponse } from '../models/admin-api.types';

@Injectable({ providedIn: 'root' })
export class AdminAgenceService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/admin/agences`;

  listerToutes(): Observable<AgenceResponse[]> {
    return this.http.get<ApiRetour<AgenceResponse[]>>(this.base).pipe(map((r) => r.data));
  }

  approuver(id: string): Observable<AgenceResponse> {
    return this.http
      .patch<ApiRetour<AgenceResponse>>(`${this.base}/${id}/approve`, {})
      .pipe(map((r) => r.data));
  }

  rejeter(id: string): Observable<AgenceResponse> {
    return this.http
      .patch<ApiRetour<AgenceResponse>>(`${this.base}/${id}/reject`, {})
      .pipe(map((r) => r.data));
  }

  suspendre(id: string): Observable<AgenceResponse> {
    return this.http
      .patch<ApiRetour<AgenceResponse>>(`${this.base}/${id}/suspend`, {})
      .pipe(map((r) => r.data));
  }
}
