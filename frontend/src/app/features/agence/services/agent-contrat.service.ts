import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { ContratResponse, StatutBail } from '../../admin/models/admin-api.types';

export type AgentContratUpdatePayload = {
  dateDebut?: string | null;
  dateFin?: string | null;
  loyerHC?: number | null;
  charges?: number | null;
  dateSignature?: string | null;
  documentUrl?: string | null;
};

@Injectable({ providedIn: 'root' })
export class AgentContratService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/agent/contrats`;

  listContrats(statut?: StatutBail | null): Observable<ContratResponse[]> {
    let params = new HttpParams();
    if (statut) {
      params = params.set('statut', statut);
    }
    return this.http
      .get<ApiRetour<ContratResponse[]>>(this.base, { params })
      .pipe(map((r) => r.data ?? []));
  }

  getContrat(id: string): Observable<ContratResponse> {
    return this.http
      .get<ApiRetour<ContratResponse>>(`${this.base}/${id}`)
      .pipe(map((r) => r.data));
  }

  updateContrat(id: string, payload: AgentContratUpdatePayload): Observable<ContratResponse> {
    return this.http
      .patch<ApiRetour<ContratResponse>>(`${this.base}/${id}`, payload)
      .pipe(map((r) => r.data));
  }

  updateContratStatut(id: string, statut: StatutBail): Observable<ContratResponse> {
    return this.http
      .patch<ApiRetour<ContratResponse>>(`${this.base}/${id}/statut`, { statut })
      .pipe(map((r) => r.data));
  }
}
