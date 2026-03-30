import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { AgenceModificationDemandeAdminResponse } from '../models/admin-api.types';

@Injectable({ providedIn: 'root' })
export class AdminAgenceModificationService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/admin/agences/modifications`;

  listerEnAttente(): Observable<AgenceModificationDemandeAdminResponse[]> {
    return this.http
      .get<ApiRetour<AgenceModificationDemandeAdminResponse[]>>(`${this.base}/pending`)
      .pipe(map((r) => r.data ?? []));
  }

  approuver(id: string): Observable<AgenceModificationDemandeAdminResponse> {
    return this.http
      .patch<ApiRetour<AgenceModificationDemandeAdminResponse>>(`${this.base}/${id}/approve`, {})
      .pipe(map((r) => r.data as AgenceModificationDemandeAdminResponse));
  }

  rejeter(id: string, commentaire?: string): Observable<AgenceModificationDemandeAdminResponse> {
    return this.http
      .patch<ApiRetour<AgenceModificationDemandeAdminResponse>>(`${this.base}/${id}/reject`, {
        commentaire: commentaire?.trim() || null
      })
      .pipe(map((r) => r.data as AgenceModificationDemandeAdminResponse));
  }
}
