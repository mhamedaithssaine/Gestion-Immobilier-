import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { getApiErrorMessage } from '../../../../core/http/error-message.util';
import { AgentDashboardOverviewResponse } from '../../../admin/models/admin-api.types';
import { AgentDashboardService } from '../../services/agent-dashboard.service';

@Component({
  selector: 'app-agence-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './agence-dashboard.component.html',
  styleUrl: './agence-dashboard.component.scss'
})
export class AgenceDashboardComponent implements OnInit {
  private readonly api = inject(AgentDashboardService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly data = signal<AgentDashboardOverviewResponse | null>(null);

  annee = new Date().getFullYear();
  mois = new Date().getMonth() + 1;

  readonly moisOptions = Array.from({ length: 12 }, (_, i) => ({
    value: i + 1,
    label: new Date(2000, i, 1).toLocaleString('fr-FR', { month: 'long' })
  }));

  readonly anneesOptions = [this.annee, this.annee - 1, this.annee - 2, this.annee - 3];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .overview(this.annee, this.mois)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (d) =>
          this.data.set({
            ...d,
            biensDisponiblesSousMandat: d.biensDisponiblesSousMandat ?? 0,
            biensLouesSousMandat: d.biensLouesSousMandat ?? 0,
            biensVendusSousMandat: d.biensVendusSousMandat ?? 0,
            biensSousCompromisSousMandat: d.biensSousCompromisSousMandat ?? 0,
            demandeModificationAgenceEnAttente: d.demandeModificationAgenceEnAttente ?? false,
            typeDemandeModificationAgenceEnAttente: d.typeDemandeModificationAgenceEnAttente ?? null,
            resumeDemandeModificationAgenceEnAttente: d.resumeDemandeModificationAgenceEnAttente ?? null,
            derniereNoteAdminDemandeAgence: d.derniereNoteAdminDemandeAgence ?? null,
            derniereNoteAdminDemandeAgenceLe: d.derniereNoteAdminDemandeAgenceLe ?? null
          }),
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Impossible de charger le tableau de bord.'))
      });
  }

  formatDh(n: number | undefined): string {
    if (n == null || Number.isNaN(n)) return '—';
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(n)} DH`;
  }

  totalBiensSousMandat(d: {
    biensDisponiblesSousMandat: number;
    biensLouesSousMandat: number;
    biensVendusSousMandat: number;
    biensSousCompromisSousMandat: number;
  }): number {
    return (
      d.biensDisponiblesSousMandat +
      d.biensLouesSousMandat +
      d.biensVendusSousMandat +
      d.biensSousCompromisSousMandat
    );
  }

  pct(part: number, total: number): number {
    if (total <= 0) return 0;
    return Math.round((part / total) * 1000) / 10;
  }
}
