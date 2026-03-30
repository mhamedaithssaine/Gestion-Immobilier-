import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { BienResponse, ContratResponse, MandatResponse, StatutBail } from '../../admin/models/admin-api.types';

export type ProprietaireBienCreatePayload = {
  type: 'APPARTEMENT' | 'MAISON';
  titre: string;
  surface: number;
  prixBase: number;
  statut: 'DISPONIBLE' | 'LOUE' | 'VENDU' | 'SOUS_COMPROMIS';
  adresse: {
    rue: string;
    ville: string;
    codePostal: string;
    pays: string;
  };
  etage?: number;
  ascenseur?: boolean;
  surfaceTerrain?: number;
  garage?: boolean;
};

export type ProprietaireContratUpdatePayload = {
  dateDebut?: string | null;
  dateFin?: string | null;
  loyerHC?: number | null;
  charges?: number | null;
  dateSignature?: string | null;
  documentUrl?: string | null;
};

@Injectable({ providedIn: 'root' })
export class ProprietaireEspaceService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/proprietaire`;

  listBiens(): Observable<BienResponse[]> {
    return this.http
      .get<ApiRetour<BienResponse[]>>(`${this.base}/biens`)
      .pipe(map((r) => r.data ?? []));
  }

  createBien(payload: ProprietaireBienCreatePayload, images: File[] = []): Observable<BienResponse> {
    if (!images.length) {
      return this.http
        .post<ApiRetour<BienResponse>>(`${this.base}/biens`, payload)
        .pipe(map((r) => r.data));
    }

    const formData = new FormData();
    formData.append('data', JSON.stringify(payload));
    images.forEach((file) => formData.append('images', file, file.name));
    return this.http
      .post<ApiRetour<BienResponse>>(`${this.base}/biens`, formData)
      .pipe(map((r) => r.data));
  }

  listContrats(): Observable<ContratResponse[]> {
    return this.http
      .get<ApiRetour<ContratResponse[]>>(`${this.base}/contrats`)
      .pipe(map((r) => r.data ?? []));
  }

  listMandats(): Observable<MandatResponse[]> {
    return this.http
      .get<ApiRetour<MandatResponse[]>>(`${this.base}/mandats`)
      .pipe(map((r) => r.data ?? []));
  }

  getMandat(id: string): Observable<MandatResponse> {
    return this.http
      .get<ApiRetour<MandatResponse>>(`${this.base}/mandats/${id}`)
      .pipe(map((r) => r.data as MandatResponse));
  }

  deleteBien(id: string): Observable<void> {
    return this.http.delete<ApiRetour<null>>(`${this.base}/biens/${id}`).pipe(map(() => undefined));
  }

  getBien(id: string): Observable<BienResponse> {
    return this.http
      .get<ApiRetour<BienResponse>>(`${this.base}/biens/${id}`)
      .pipe(map((r) => r.data));
  }

  updateBien(id: string, payload: ProprietaireBienCreatePayload, images: File[] = []): Observable<BienResponse> {
    if (!images.length) {
      return this.http
        .put<ApiRetour<BienResponse>>(`${this.base}/biens/${id}`, payload)
        .pipe(map((r) => r.data));
    }
    const formData = new FormData();
    formData.append('data', JSON.stringify(payload));
    images.forEach((file) => formData.append('images', file, file.name));
    return this.http
      .put<ApiRetour<BienResponse>>(`${this.base}/biens/${id}`, formData)
      .pipe(map((r) => r.data));
  }

  resilierContrat(id: string): Observable<void> {
    return this.http
      .patch<ApiRetour<ContratResponse>>(`${this.base}/contrats/${id}/resilier`, {})
      .pipe(map(() => undefined));
  }

  updateContrat(id: string, payload: ProprietaireContratUpdatePayload): Observable<ContratResponse> {
    return this.http
      .patch<ApiRetour<ContratResponse>>(`${this.base}/contrats/${id}`, payload)
      .pipe(map((r) => r.data));
  }

  updateContratStatut(id: string, statut: StatutBail): Observable<ContratResponse> {
    return this.http
      .patch<ApiRetour<ContratResponse>>(`${this.base}/contrats/${id}/statut`, { statut })
      .pipe(map((r) => r.data));
  }

  demanderResiliationMandat(id: string): Observable<void> {
    return this.http
      .patch<ApiRetour<MandatResponse>>(`${this.base}/mandats/${id}/demande-resiliation`, {})
      .pipe(map(() => undefined));
  }

  downloadMandatDocument(id: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.base}/mandats/${id}/document`, {
      responseType: 'blob',
      observe: 'response'
    });
  }
}

