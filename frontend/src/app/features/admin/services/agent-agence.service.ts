import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { AgenceEspaceAgentResponse } from '../models/admin-api.types';

export interface UpdateAgencePayload {
  nom: string;
  email: string;
  telephone: string;
  adresse: string;
  ville: string;
}

@Injectable({ providedIn: 'root' })
export class AgentAgenceService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/agent/agence`;

  getMonAgence(): Observable<AgenceEspaceAgentResponse> {
    return this.http
      .get<ApiRetour<AgenceEspaceAgentResponse>>(`${this.base}/me`)
      .pipe(map((r) => r.data as AgenceEspaceAgentResponse));
  }

  /** Soumet une demande de mise à jour (validation admin requise). */
  soumettreDemandeMiseAJour(payload: UpdateAgencePayload): Observable<AgenceEspaceAgentResponse> {
    return this.http
      .post<ApiRetour<AgenceEspaceAgentResponse>>(`${this.base}/me/demandes/mise-a-jour`, payload)
      .pipe(map((r) => r.data as AgenceEspaceAgentResponse));
  }

  /** Demande de clôture : après accord admin, agence suspendue et comptes agents désactivés. */
  soumettreDemandeSuppression(): Observable<AgenceEspaceAgentResponse> {
    return this.http
      .post<ApiRetour<AgenceEspaceAgentResponse>>(`${this.base}/me/demandes/suppression`, {})
      .pipe(map((r) => r.data as AgenceEspaceAgentResponse));
  }
}
