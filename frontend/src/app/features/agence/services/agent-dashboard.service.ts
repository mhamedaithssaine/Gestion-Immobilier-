import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { AgentDashboardOverviewResponse } from '../../admin/models/admin-api.types';

@Injectable({ providedIn: 'root' })
export class AgentDashboardService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/agent/dashboard`;

  overview(annee: number, mois: number): Observable<AgentDashboardOverviewResponse> {
    const params = new HttpParams().set('annee', String(annee)).set('mois', String(mois));
    return this.http
      .get<ApiRetour<AgentDashboardOverviewResponse>>(`${this.base}/overview`, { params })
      .pipe(map((r) => r.data as AgentDashboardOverviewResponse));
  }
}
