import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import {
  BiensLouesVsLibresResponse,
  LocatairesEnRetardResponse,
  MandatsGestionStatistiqueResponse,
  NombreBiensDisponiblesResponse,
  RevenusMensuelsResponse
} from '../models/admin-api.types';

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/admin/dashboard`;

  nombreBiensDisponibles(): Observable<NombreBiensDisponiblesResponse> {
    return this.http
      .get<ApiRetour<NombreBiensDisponiblesResponse>>(`${this.base}/nombre-biens-disponibles`)
      .pipe(map((r) => r.data));
  }

  biensLouesVsLibres(): Observable<BiensLouesVsLibresResponse> {
    return this.http
      .get<ApiRetour<BiensLouesVsLibresResponse>>(`${this.base}/biens-loues-vs-libres`)
      .pipe(map((r) => r.data));
  }

  revenusMensuels(annee: number, mois: number): Observable<RevenusMensuelsResponse> {
    return this.http
      .get<ApiRetour<RevenusMensuelsResponse>>(`${this.base}/revenus-mensuels`, {
        params: { annee: String(annee), mois: String(mois) }
      })
      .pipe(map((r) => r.data));
  }

  locatairesEnRetard(annee: number, mois: number): Observable<LocatairesEnRetardResponse> {
    return this.http
      .get<ApiRetour<LocatairesEnRetardResponse>>(`${this.base}/locataires-en-retard`, {
        params: { annee: String(annee), mois: String(mois) }
      })
      .pipe(map((r) => r.data));
  }

  mandatsToutesAgences(): Observable<MandatsGestionStatistiqueResponse[]> {
    return this.http
      .get<ApiRetour<MandatsGestionStatistiqueResponse[]>>(
        `${this.base}/agences/stats/mandats-gestion`
      )
      .pipe(map((r) => r.data));
  }
}
