import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { getApiErrorMessage } from '../../core/http/error-message.util';
import { ProprietaireDashboardOverviewResponse } from './models/proprietaire-dashboard.types';
import { ProprietaireDashboardService } from './services/proprietaire-dashboard.service';

@Component({
  selector: 'app-proprietaire-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './proprietaire-dashboard.component.html',
  styleUrl: './proprietaire-dashboard.component.scss'
})
export class ProprietaireDashboardComponent implements OnInit {
  private readonly api = inject(ProprietaireDashboardService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly data = signal<ProprietaireDashboardOverviewResponse | null>(null);

  annee = new Date().getFullYear();
  mois = new Date().getMonth() + 1;

  readonly moisOptions = Array.from({ length: 12 }, (_, i) => ({
    value: i + 1,
    label: new Date(2000, i, 1).toLocaleString('fr-FR', { month: 'long' })
  }));

  readonly anneesOptions = [this.annee, this.annee - 1, this.annee - 2, this.annee - 3];

  readonly maxHistoriqueMontant = computed(() => {
    const rows = this.data()?.historiqueRevenus6Mois ?? [];
    const m = Math.max(0, ...rows.map((r) => r.montant ?? 0));
    return m > 0 ? m : 1;
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .overview(this.annee, this.mois)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (d) => this.data.set(d),
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Impossible de charger le tableau de bord.'))
      });
  }

  formatDh(n: number | undefined): string {
    if (n == null || Number.isNaN(n)) return '—';
    return `${new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(n)} DH`;
  }

  labelMoisCourt(mois: number): string {
    return new Date(2000, mois - 1, 1).toLocaleString('fr-FR', { month: 'short' });
  }

  totalBiensPatrimoine(d: ProprietaireDashboardOverviewResponse): number {
    return (
      d.biensDisponibles +
      d.biensLoues +
      d.biensVendus +
      d.biensSousCompromis
    );
  }

  pct(part: number, total: number): number {
    if (total <= 0) return 0;
    return Math.round((part / total) * 1000) / 10;
  }

  barHeightPct(montant: number): number {
    const max = this.maxHistoriqueMontant();
    return Math.max(4, Math.round(((montant ?? 0) / max) * 100));
  }
}
