import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { BienResponse, MandatResponse, ProprietaireResponse, StatutMandat, UtilisateurResponse } from '../models/admin-api.types';
import { Page } from './admin-user.service';

export type CreateMandatPayload = {
  bienId: string;
  proprietaireId: string;
  agentId: string;
  dateDebut: string;
  dateFin: string;
  commissionPct?: number | null;
  dateSignature?: string | null;
  documentUrl?: string | null;
};

export type UpdateMandatPayload = {
  dateDebut?: string | null;
  dateFin?: string | null;
  commissionPct?: number | null;
  dateSignature?: string | null;
};

@Injectable({ providedIn: 'root' })
export class MandatManagementService {
  private readonly http = inject(HttpClient);
  private readonly mandatsBase = `${environment.apiBaseUrl}/api/admin/mandats`;
  private readonly biensBase = `${environment.apiBaseUrl}/api/proprietaire/biens`;
  private readonly usersBase = `${environment.apiBaseUrl}/api/admin/users/paged`;

  listMandats(filters?: { statut?: StatutMandat | 'ALL'; proprietaireId?: string; bienId?: string }): Observable<MandatResponse[]> {
    let params = new HttpParams();
    if (filters?.statut && filters.statut !== 'ALL') params = params.set('statut', filters.statut);
    if (filters?.proprietaireId) params = params.set('proprietaireId', filters.proprietaireId);
    if (filters?.bienId) params = params.set('bienId', filters.bienId);
    return this.http
      .get<ApiRetour<MandatResponse[]>>(this.mandatsBase, { params })
      .pipe(map((r) => r.data ?? []));
  }

  getMandat(id: string): Observable<MandatResponse> {
    return this.http
      .get<ApiRetour<MandatResponse>>(`${this.mandatsBase}/${id}`)
      .pipe(map((r) => r.data as MandatResponse));
  }

  updateMandat(id: string, payload: UpdateMandatPayload): Observable<MandatResponse> {
    return this.http
      .patch<ApiRetour<MandatResponse>>(`${this.mandatsBase}/${id}`, payload)
      .pipe(map((r) => r.data as MandatResponse));
  }

  updateMandatMultipart(id: string, payload: UpdateMandatPayload, document: File): Observable<MandatResponse> {
    const formData = new FormData();
    formData.append('data', JSON.stringify(payload));
    formData.append('document', document, document.name);
    return this.http
      .patch<ApiRetour<MandatResponse>>(`${this.mandatsBase}/${id}`, formData)
      .pipe(map((r) => r.data as MandatResponse));
  }

  downloadMandatDocument(id: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.mandatsBase}/${id}/document`, {
      responseType: 'blob',
      observe: 'response'
    });
  }

  approuverResiliation(id: string): Observable<MandatResponse> {
    return this.http
      .patch<ApiRetour<MandatResponse>>(`${this.mandatsBase}/${id}/approuver-resiliation`, {})
      .pipe(map((r) => r.data as MandatResponse));
  }

  rejeterDemandeResiliation(id: string): Observable<MandatResponse> {
    return this.http
      .patch<ApiRetour<MandatResponse>>(`${this.mandatsBase}/${id}/rejeter-demande-resiliation`, {})
      .pipe(map((r) => r.data as MandatResponse));
  }

  createMandat(payload: CreateMandatPayload): Observable<MandatResponse> {
    return this.createMandatMultipart(payload);
  }

  createMandatMultipart(payload: CreateMandatPayload, document?: File | null): Observable<MandatResponse> {
    const formData = new FormData();
    formData.append('data', JSON.stringify(payload));
    if (document) formData.append('document', document, document.name);
    return this.http
      .post<ApiRetour<MandatResponse>>(this.mandatsBase, formData)
      .pipe(map((r) => r.data));
  }

  resilierMandat(id: string): Observable<MandatResponse> {
    return this.http
      .patch<ApiRetour<MandatResponse>>(`${this.mandatsBase}/${id}/resilier`, {})
      .pipe(map((r) => r.data));
  }

  listBiensVisibles(): Observable<BienResponse[]> {
    return this.http
      .get<ApiRetour<BienResponse[]>>(this.biensBase)
      .pipe(map((r) => r.data ?? []));
  }

  listBiensByProprietaire(proprietaireId: string): Observable<BienResponse[]> {
    return this.http
      .get<ApiRetour<BienResponse[]>>(`${this.mandatsBase}/proprietaires/${proprietaireId}/biens`)
      .pipe(map((r) => r.data ?? []));
  }

  listUsers(max = 300): Observable<UtilisateurResponse[]> {
    return this.http
      .get<ApiRetour<Page<UtilisateurResponse>>>(this.usersBase, {
        params: { sync: 'true', page: '0', size: String(max) }
      })
      .pipe(map((r) => r.data?.content ?? []));
  }

  listAgents(max = 300): Observable<UtilisateurResponse[]> {
    return this.listUsers(max).pipe(
      map((users) => users.filter((u) => u.roles?.includes('ROLE_AGENT')))
    );
  }

  listProprietaires(max = 300): Observable<ProprietaireResponse[]> {
    return this.listUsers(max).pipe(
      map((users) =>
        users
          .filter((u) => u.roles?.includes('ROLE_PROPRIETAIRE'))
          .map(
            (u) =>
              ({
                ...u,
                rib: null,
                adresseContact: null
              }) as ProprietaireResponse
          )
      )
    );
  }
}
