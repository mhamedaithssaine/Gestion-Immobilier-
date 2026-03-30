import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../../core/http/error-message.util';
import { ContratResponse, StatutBail } from '../../../admin/models/admin-api.types';
import {
  ContratEditSubmitEvent,
  ContratEditModalComponent,
  ContratStatusSubmitEvent
} from '../../components/contrat-edit-modal/contrat-edit-modal.component';
import { ProprietaireEspaceService } from '../../services/proprietaire-espace.service';

@Component({
  selector: 'app-proprietaire-contrats',
  standalone: true,
  imports: [CommonModule, FormsModule, ContratEditModalComponent],
  templateUrl: './proprietaire-contrats.component.html',
  styleUrl: './proprietaire-contrats.component.scss'
})
export class ProprietaireContratsComponent implements OnInit {
  private readonly api = inject(ProprietaireEspaceService);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<ContratResponse[]>([]);
  readonly selected = signal<ContratResponse | null>(null);
  readonly editing = signal<ContratResponse | null>(null);

  readonly filterStatut = signal<'ALL' | StatutBail>('ALL');
  readonly filterQuery = signal('');

  readonly statutOptions: { value: 'ALL' | StatutBail; label: string }[] = [
    { value: 'ALL', label: 'Tous les statuts' },
    { value: 'ACTIF', label: 'Actif' },
    { value: 'EN_ATTENTE', label: 'En attente (vous)' },
    { value: 'EN_ATTENTE_VALIDATION_AGENT', label: 'En attente (agent)' },
    { value: 'REFUSE', label: 'Refusé' },
    { value: 'RESILIE', label: 'Résilié' },
    { value: 'TERMINE', label: 'Terminé' }
  ];

  readonly rowsFiltered = computed(() => {
    const s = this.filterStatut();
    const q = this.filterQuery().trim().toLowerCase();
    return this.rows().filter((c) => {
      if (s !== 'ALL' && c.statut !== s) return false;
      if (!q) return true;
      const pool = `${c.numContrat} ${c.bien.reference} ${c.locataire.firstName} ${c.locataire.lastName}`.toLowerCase();
      return pool.includes(q);
    });
  });

  readonly totalCount = computed(() => this.rows().length);
  readonly actifsCount = computed(() => this.rows().filter((c) => c.statut === 'ACTIF').length);
  readonly attenteCount = computed(
    () =>
      this.rows().filter((c) => c.statut === 'EN_ATTENTE' || c.statut === 'EN_ATTENTE_VALIDATION_AGENT').length
  );
  readonly resiliesCount = computed(() => this.rows().filter((c) => c.statut === 'RESILIE').length);
  readonly valeurMensuelle = computed(() =>
    this.rows()
      .filter((c) => c.statut === 'ACTIF' || c.statut === 'EN_ATTENTE')
      .reduce((sum, c) => sum + (c.loyerHC ?? 0) + (c.charges ?? 0), 0)
  );

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .listContrats()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (rows) => this.rows.set(rows),
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Chargement impossible des contrats.'))
      });
  }

  statutLabel(s: StatutBail): string {
    return (
      {
        ACTIF: 'Actif',
        RESILIE: 'Résilié',
        TERMINE: 'Terminé',
        EN_ATTENTE: 'En attente (vous)',
        EN_ATTENTE_VALIDATION_AGENT: 'En attente (agent)',
        REFUSE: 'Refusé'
      }[s] ?? s
    );
  }

  statutClass(s: StatutBail): string {
    return (
      {
        ACTIF: 'is-actif',
        EN_ATTENTE: 'is-attente',
        EN_ATTENTE_VALIDATION_AGENT: 'is-agent',
        REFUSE: 'is-refuse',
        RESILIE: 'is-resilie',
        TERMINE: 'is-termine'
      }[s] ?? ''
    );
  }

  canResilier(c: ContratResponse): boolean {
    if (c.statut === 'REFUSE' || c.statut === 'EN_ATTENTE_VALIDATION_AGENT') return false;
    return c.statut === 'ACTIF' || c.statut === 'EN_ATTENTE';
  }

  openDetail(c: ContratResponse): void {
    this.selected.set(c);
  }

  closeDetail(): void {
    this.selected.set(null);
  }

  openEdit(c: ContratResponse): void {
    this.editing.set(c);
  }

  closeEdit(): void {
    this.editing.set(null);
  }

  async resilier(id: string, numContrat: string): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Resilier ce contrat ?',
      text: numContrat,
      confirmButtonText: 'Resilier',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;
    this.actionId.set(id);
    this.api
      .resilierContrat(id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succes', 'Contrat resilie.');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Resiliation impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  onSaveContrat(evt: ContratEditSubmitEvent): void {
    this.actionId.set(evt.contratId);
    this.error.set(null);
    this.api
      .updateContrat(evt.contratId, evt.payload)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', 'Contrat mis à jour.');
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

  async onSaveStatus(evt: ContratStatusSubmitEvent): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Changer le statut du contrat ?',
      text: `Nouveau statut: ${this.statutLabel(evt.statut)}`,
      confirmButtonText: 'Confirmer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(evt.contratId);
    this.error.set(null);
    this.api
      .updateContratStatut(evt.contratId, evt.statut)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', 'Statut du contrat mis à jour.');
          this.closeEdit();
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Changement de statut impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }
}

