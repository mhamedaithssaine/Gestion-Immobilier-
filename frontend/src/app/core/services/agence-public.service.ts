import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiRetour } from '../http/api-types';

export type CreateAgencePayload = {
  nom: string;
  email: string;
  telephone: string;
  adresse: string;
  ville: string;
  agentUsername: string;
  agentEmail: string;
  agentFirstName: string;
  agentLastName: string;
  agentPassword: string;
};

@Injectable({ providedIn: 'root' })
export class AgencePublicService {
  private readonly http = inject(HttpClient);

  inscrire(payload: CreateAgencePayload): Observable<ApiRetour<unknown>> {
    return this.http.post<ApiRetour<unknown>>(
      `${environment.apiBaseUrl}/api/public/agences/inscription`,
      payload
    );
  }
}

