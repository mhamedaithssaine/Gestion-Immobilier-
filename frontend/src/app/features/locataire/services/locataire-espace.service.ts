import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { BienResponse, ContratResponse } from '../../admin/models/admin-api.types';

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface BienFilters {
  q?: string;
  ville?: string;
  minPrix?: number | null;
  maxPrix?: number | null;
  minSurface?: number | null;
  maxSurface?: number | null;
  /** Ex. APPARTEMENT, MAISON — plusieurs valeurs = filtre OU */
  types?: string[];
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class LocataireEspaceService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/locataire`;

  listBiens(filters: BienFilters): Observable<SpringPage<BienResponse>> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 8));

    const mapValues: Record<string, string | undefined> = {
      q: filters.q?.trim() || undefined,
      ville: filters.ville?.trim() || undefined,
      minPrix: filters.minPrix != null ? String(filters.minPrix) : undefined,
      maxPrix: filters.maxPrix != null ? String(filters.maxPrix) : undefined,
      minSurface: filters.minSurface != null ? String(filters.minSurface) : undefined,
      maxSurface: filters.maxSurface != null ? String(filters.maxSurface) : undefined
    };
    Object.entries(mapValues).forEach(([k, v]) => {
      if (v !== undefined) params = params.set(k, v);
    });
    (filters.types ?? []).forEach((t) => {
      const s = t?.trim();
      if (s) params = params.append('types', s);
    });

    return this.http
      .get<ApiRetour<SpringPage<BienResponse>>>(`${this.base}/biens`, { params })
      .pipe(map((r) => r.data));
  }

  getBien(id: string): Observable<BienResponse> {
    return this.http.get<ApiRetour<BienResponse>>(`${this.base}/biens/${id}`).pipe(map((r) => r.data));
  }

  demanderLocation(id: string, dateDebut: string, dateFin: string): Observable<ContratResponse> {
    return this.http
      .post<ApiRetour<ContratResponse>>(`${this.base}/biens/${id}/demande-location`, { dateDebut, dateFin })
      .pipe(map((r) => r.data));
  }

  listMesDemandes(): Observable<ContratResponse[]> {
    return this.http
      .get<ApiRetour<ContratResponse[]>>(`${this.base}/mes-demandes`)
      .pipe(map((r) => r.data ?? []));
  }
}

