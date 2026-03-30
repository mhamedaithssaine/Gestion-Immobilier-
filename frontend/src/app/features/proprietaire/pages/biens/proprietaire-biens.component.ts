import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../../core/http/error-message.util';
import { BienResponse, ContratResponse, StatutBien } from '../../../admin/models/admin-api.types';
import { FormsModule } from '@angular/forms';
import { ProprietaireEspaceService } from '../../services/proprietaire-espace.service';
import { BienCreateFormComponent, BienFormSubmitEvent } from '../../components/bien-create-form/bien-create-form.component';
import { BienDetailModalComponent } from '../../components/bien-detail-modal/bien-detail-modal.component';

@Component({
  selector: 'app-proprietaire-biens',
  standalone: true,
  imports: [CommonModule, FormsModule, BienCreateFormComponent, BienDetailModalComponent],
  templateUrl: './proprietaire-biens.component.html',
  styleUrl: './proprietaire-biens.component.scss'
})
export class ProprietaireBiensComponent implements OnInit {
  private readonly espaceApi = inject(ProprietaireEspaceService);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<BienResponse[]>([]);
  readonly contratsByBien = signal<Record<string, ContratResponse[]>>({});
  readonly saving = signal(false);
  readonly loadingEditId = signal<string | null>(null);
  readonly showCreate = signal(false);
  readonly selectedBien = signal<BienResponse | null>(null);
  readonly editDetail = signal<BienResponse | null>(null);

  filterStatut = signal<'ALL' | StatutBien>('ALL');
  filterSearch = signal('');

  readonly statutOptions: { value: 'ALL' | StatutBien; label: string }[] = [
    { value: 'ALL', label: 'Tous les statuts' },
    { value: 'DISPONIBLE', label: 'Disponible' },
    { value: 'LOUE', label: 'Loué' },
    { value: 'VENDU', label: 'Vendu' },
    { value: 'SOUS_COMPROMIS', label: 'Sous compromis' }
  ];

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      biens: this.espaceApi.listBiens(),
      contrats: this.espaceApi.listContrats()
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ biens, contrats }) => {
          this.rows.set(biens);
          const byBien: Record<string, ContratResponse[]> = {};
          contrats.forEach((c) => {
            const id = c.bien?.id;
            if (!id) return;
            byBien[id] = byBien[id] ? [...byBien[id], c] : [c];
          });
          this.contratsByBien.set(byBien);
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Impossible de charger vos biens.'))
      });
  }

  filteredRows(): BienResponse[] {
    const s = this.filterStatut();
    const q = this.filterSearch().trim().toLowerCase();
    return this.rows().filter((b) => {
      if (s !== 'ALL' && b.statut !== s) return false;
      if (!q) return true;
      const pool = `${b.reference} ${b.titre} ${b.adresse?.ville ?? ''} ${b.adresse?.rue ?? ''}`.toLowerCase();
      return pool.includes(q);
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

  locataireLabelForBien(bienId: string): string {
    const contrats = this.contratsByBien()[bienId] ?? [];
    const active = contrats.find((c) => c.statut === 'ACTIF') ?? contrats.find((c) => c.statut === 'EN_ATTENTE');
    if (!active) return 'Aucun';
    return `${active.locataire.firstName} ${active.locataire.lastName}`.trim();
  }

  async deleteBien(id: string, reference: string): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Supprimer ce bien ?',
      text: `${reference} — action irréversible`,
      confirmButtonText: 'Supprimer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(id);
    this.espaceApi
      .deleteBien(id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Supprimé', 'Bien supprimé.');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Suppression impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  onBienFormSubmit(e: BienFormSubmitEvent): void {
    if (e.mode === 'create') {
      this.saving.set(true);
      this.error.set(null);
      this.espaceApi
        .createBien(e.payload, e.images)
        .pipe(finalize(() => this.saving.set(false)))
        .subscribe({
          next: async () => {
            await this.alert.success('Créé', 'Bien ajouté avec succès.');
            this.showCreate.set(false);
            this.reload();
          },
          error: async (err: unknown) => {
            const msg = getApiErrorMessage(err, 'Création du bien impossible.');
            this.error.set(msg);
            await this.alert.error('Erreur', msg);
          }
        });
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.espaceApi
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

  openEdit(bienId: string): void {
    this.loadingEditId.set(bienId);
    this.error.set(null);
    this.espaceApi
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

  openDetail(bienId: string): void {
    this.error.set(null);
    this.espaceApi.getBien(bienId).subscribe({
      next: (b) => this.selectedBien.set(b),
      error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Impossible de charger le detail du bien.'))
    });
  }

  closeDetail(): void {
    this.selectedBien.set(null);
  }
}

