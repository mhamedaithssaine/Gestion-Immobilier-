import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { BienResponse, StatutBien } from '../models/admin-api.types';
import { AdminBienService } from '../services/admin-bien.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-biens',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-biens.component.html',
  styleUrl: './admin-biens.component.scss'
})
export class AdminBiensComponent implements OnInit {
  private readonly biensApi = inject(AdminBienService);
  private readonly alert = inject(AlertService);
  private readonly auth = inject(AuthService);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<BienResponse[]>([]);

  filterStatut = signal<'ALL' | StatutBien>('ALL');
  filterSearch = signal('');

  readonly isAdmin = computed(() => this.auth.hasRole('ROLE_ADMIN'));

  readonly statutOptions: { value: 'ALL' | StatutBien; label: string }[] = [
    { value: 'ALL', label: 'Tous les statuts' },
    { value: 'DISPONIBLE', label: 'Disponible' },
    { value: 'LOUE', label: 'Loué' },
    { value: 'VENDU', label: 'Vendu' },
    { value: 'SOUS_COMPROMIS', label: 'Sous compromis' }
  ];

  readonly filteredRows = computed(() => {
    const s = this.filterStatut();
    const q = this.filterSearch().trim().toLowerCase();
    return this.rows().filter((b) => {
      if (s !== 'ALL' && b.statut !== s) return false;
      if (!q) return true;
      const pool =
        `${b.reference} ${b.titre} ${b.proprietaireNom} ${b.adresse?.ville ?? ''} ${b.adresse?.rue ?? ''}`.toLowerCase();
      return pool.includes(q);
    });
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.biensApi
      .list()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (list) => this.rows.set(list),
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Impossible de charger les biens.'))
      });
  }

  labelStatut(s: StatutBien): string {
    const m: Record<StatutBien, string> = {
      DISPONIBLE: 'Disponible',
      LOUE: 'Loué',
      VENDU: 'Vendu',
      SOUS_COMPROMIS: 'Sous compromis'
    };
    return m[s] ?? s;
  }

  async deleteBien(id: string, reference: string): Promise<void> {
    if (!this.isAdmin()) return;
    const ok = await this.alert.confirm({
      title: 'Supprimer ce bien ?',
      text: `${reference} — action irréversible`,
      confirmButtonText: 'Supprimer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(id);
    this.biensApi
      .delete(id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Supprimé', 'Bien supprimé avec succès.');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Suppression impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }
}

