import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { getApiErrorMessage } from '../../../../core/http/error-message.util';
import { AlertService } from '../../../../core/services/alert.service';
import { BienResponse, MandatResponse, StatutBien } from '../../../admin/models/admin-api.types';
import {
  BienCreateFormComponent,
  BienFormSubmitEvent
} from '../../../proprietaire/components/bien-create-form/bien-create-form.component';
import { AgentBienService } from '../../services/agent-bien.service';
import { AgentMandatService } from '../../services/agent-mandat.service';
export type AgenceBienRow = {
  bien: BienResponse;
  mandat: MandatResponse | null;
};

@Component({
  selector: 'app-agence-biens',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, BienCreateFormComponent],
  templateUrl: './agence-biens.component.html',
  styleUrl: './agence-biens.component.scss'
})
export class AgenceBiensComponent implements OnInit {
  private readonly biensApi = inject(AgentBienService);
  private readonly mandatsApi = inject(AgentMandatService);
  private readonly router = inject(Router);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly loadingEditId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<AgenceBienRow[]>([]);
  readonly detail = signal<BienResponse | null>(null);
  readonly editDetail = signal<BienResponse | null>(null);

  readonly filterStatut = signal<'ALL' | StatutBien>('ALL');
  readonly filterQuery = signal('');

  readonly statutOptions: { value: 'ALL' | StatutBien; label: string }[] = [
    { value: 'ALL', label: 'Tous les statuts' },
    { value: 'DISPONIBLE', label: 'Disponible' },
    { value: 'LOUE', label: 'Loué' },
    { value: 'VENDU', label: 'Vendu' },
    { value: 'SOUS_COMPROMIS', label: 'Sous compromis' }
  ];

  readonly filteredRows = computed(() => {
    const s = this.filterStatut();
    const q = this.filterQuery().trim().toLowerCase();
    return this.rows().filter(({ bien: b, mandat: m }) => {
      if (s !== 'ALL' && b.statut !== s) return false;
      if (!q) return true;
      const pool =
        `${b.reference} ${b.titre} ${b.proprietaireNom} ${m?.numMandat ?? ''}`.toLowerCase();
      return pool.includes(q);
    });
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      biens: this.biensApi.listBiens(),
      mandats: this.mandatsApi.listMandats({ statut: 'ALL' })
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ biens, mandats }) => {
          const enriched: AgenceBienRow[] = biens.map((bien) => ({
            bien,
            mandat: this.pickMandatPourBien(bien.id, mandats) ?? null
          }));
          this.rows.set(enriched);
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Chargement des biens impossible.'))
      });
  }

  /** Mandat actif prioritaire, sinon le plus récent sur ce bien. */
  private pickMandatPourBien(bienId: string, mandats: MandatResponse[]): MandatResponse | undefined {
    const list = mandats.filter((m) => m.bienId === bienId);
    if (!list.length) return undefined;
    const actif = list.find((m) => m.statut === 'ACTIF');
    if (actif) return actif;
    return [...list].sort((a, b) => (b.dateDebut ?? '').localeCompare(a.dateDebut ?? ''))[0];
  }

  statutBienLabel(s: StatutBien): string {
    return (
      {
        DISPONIBLE: 'Disponible',
        LOUE: 'Loué',
        VENDU: 'Vendu',
        SOUS_COMPROMIS: 'Sous compromis'
      }[s] ?? s
    );
  }

  mandatStatutLabel(m: MandatResponse): string {
    return (
      {
        ACTIF: 'Actif',
        EN_ATTENTE: 'En attente',
        EN_ATTENTE_RESILIATION: 'Résiliation en attente',
        RESILIE: 'Résilié',
        TERMINE: 'Terminé'
      }[m.statut] ?? m.statut
    );
  }

  openDetail(b: BienResponse): void {
    this.detail.set(b);
  }

  closeDetail(): void {
    this.detail.set(null);
  }

  allerAuMandat(mandatId: string): void {
    void this.router.navigate(['/agence/mandats'], { queryParams: { ouvrir: mandatId } });
  }

  openEdit(bienId: string): void {
    this.loadingEditId.set(bienId);
    this.error.set(null);
    this.biensApi
      .getBien(bienId)
      .pipe(finalize(() => this.loadingEditId.set(null)))
      .subscribe({
        next: (b) => this.editDetail.set(b),
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Impossible de charger le bien.'))
      });
  }

  closeEdit(): void {
    this.editDetail.set(null);
  }

  onBienFormSubmit(e: BienFormSubmitEvent): void {
    if (e.mode !== 'edit') return;
    this.saving.set(true);
    this.error.set(null);
    this.biensApi
      .updateBien(e.bienId, e.payload, e.images)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: async () => {
          await this.alert.success('Enregistré', 'Bien mis à jour.');
          this.closeEdit();
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Mise à jour impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }
}
