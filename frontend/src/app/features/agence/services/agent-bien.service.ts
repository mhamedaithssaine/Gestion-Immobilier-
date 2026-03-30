import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { BienResponse } from '../../admin/models/admin-api.types';
import { ProprietaireBienCreatePayload } from '../../proprietaire/services/proprietaire-espace.service';

@Injectable({ providedIn: 'root' })
export class AgentBienService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/agent/biens`;

  listBiens(): Observable<BienResponse[]> {
    return this.http.get<ApiRetour<BienResponse[]>>(this.base).pipe(map((r) => r.data ?? []));
  }

  getBien(id: string): Observable<BienResponse> {
    return this.http.get<ApiRetour<BienResponse>>(`${this.base}/${id}`).pipe(map((r) => r.data as BienResponse));
  }

  updateBien(id: string, payload: ProprietaireBienCreatePayload, images: File[]): Observable<BienResponse> {
    if (!images.length) {
      return this.http
        .put<ApiRetour<BienResponse>>(`${this.base}/${id}`, payload)
        .pipe(map((r) => r.data as BienResponse));
    }
    const formData = new FormData();
    formData.append('data', JSON.stringify(payload));
    images.forEach((file) => formData.append('images', file, file.name));
    return this.http.put<ApiRetour<BienResponse>>(`${this.base}/${id}`, formData).pipe(map((r) => r.data as BienResponse));
  }
}