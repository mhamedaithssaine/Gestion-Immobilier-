import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { AuthService } from '../../../core/services/auth.service';
import {
  BiensLouesVsLibresResponse,
  MandatsGestionStatistiqueResponse,
  RevenusMensuelsResponse,
  LocatairesEnRetardResponse
} from '../models/admin-api.types';
import { AdminDashboardService } from '../services/admin-dashboard.service';

@Component({
  selector: 'app-admin-overview',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-overview.component.html',
  styleUrl: './admin-overview.component.scss'
})
export class AdminOverviewComponent implements OnInit {
  private readonly dashboard = inject(AdminDashboardService);
  private readonly auth = inject(AuthService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  annee = signal(new Date().getFullYear());
  mois = signal(new Date().getMonth() + 1);

  readonly nbDispo = signal<number | null>(null);
  readonly biensRatio = signal<BiensLouesVsLibresResponse | null>(null);
  readonly revenus = signal<RevenusMensuelsResponse | null>(null);
  readonly retard = signal<LocatairesEnRetardResponse | null>(null);
  readonly mandats = signal<MandatsGestionStatistiqueResponse[]>([]);
  readonly filterMandats = signal('');

  readonly isAdmin = computed(() => this.auth.hasRole('ROLE_ADMIN'));

  readonly mandatsFiltres = computed(() => {
    const q = this.filterMandats().trim().toLowerCase();
    const list = this.mandats();
    if (!q) return list;
    return list.filter((m) => m.nomAgence.toLowerCase().includes(q));
  });

  readonly anneesOptions = this.buildAnnees();
  readonly moisOptions = Array.from({ length: 12 }, (_, i) => ({
    value: i + 1,
    label: new Date(2000, i, 1).toLocaleString('fr-FR', { month: 'long' })
  }));

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    const a = this.annee();
    const m = this.mois();

    forkJoin({
      dispo: this.dashboard.nombreBiensDisponibles(),
      ratio: this.dashboard.biensLouesVsLibres(),
      rev: this.dashboard.revenusMensuels(a, m),
      retard: this.dashboard.locatairesEnRetard(a, m)
    })
      .pipe(
        switchMap((base) => {
          if (!this.auth.hasRole('ROLE_ADMIN')) {
            return of({ ...base, mandats: [] as MandatsGestionStatistiqueResponse[] });
          }
          return this.dashboard.mandatsToutesAgences().pipe(
            map((mandats) => ({ ...base, mandats }))
          );
        })
      )
      .subscribe({
        next: (res) => {
          this.nbDispo.set(res.dispo.nombreBiensDisponibles);
          this.biensRatio.set(res.ratio);
          this.revenus.set(res.rev);
          this.retard.set(res.retard);
          this.mandats.set(res.mandats);
        },
        error: (err: unknown) => {
          this.error.set(getApiErrorMessage(err, 'Impossible de charger le tableau de bord.'));
        },
        complete: () => this.loading.set(false)
      });
  }

  appliquerPeriode(): void {
    this.load();
  }

  totalBiens(r: BiensLouesVsLibresResponse | null): number {
    if (!r) return 0;
    return r.disponibles + r.loues + r.vendus + r.sousCompromis;
  }

  pct(part: number, total: number): number {
    if (total <= 0) return 0;
    return Math.round((part / total) * 1000) / 10;
  }

  formatMoney(n: number | undefined): string {
    if (n == null || Number.isNaN(n)) return '—';
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(n);
  }

  private buildAnnees(): number[] {
    const y = new Date().getFullYear();
    return [y, y - 1, y - 2, y - 3];
  }
}
