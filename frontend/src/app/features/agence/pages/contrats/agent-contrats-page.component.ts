import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../../core/http/error-message.util';
import { ContratResponse, StatutBail } from '../../../admin/models/admin-api.types';
import { AgentContratService } from '../../services/agent-contrat.service';

@Component({
  selector: 'app-agent-contrats-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agent-contrats-page.component.html',
  styleUrl: './agent-contrats-page.component.scss'
})
export class AgentContratsPageComponent implements OnInit {
  private readonly api = inject(AgentContratService);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<ContratResponse[]>([]);
  readonly filterStatut = signal<'ALL' | StatutBail>('ALL');
  readonly filterQuery = signal('');

  readonly viewContrat = signal<ContratResponse | null>(null);
  readonly viewLoading = signal(false);

  readonly editContrat = signal<ContratResponse | null>(null);
  readonly editSaving = signal(false);
  readonly statutSaving = signal(false);
  editDateDebut = '';
  editDateFin = '';
  editLoyerHC: number | null = null;
  editCharges: number | null = null;
  editDateSignature = '';
  editDocumentUrl = '';
  editStatut: StatutBail = 'EN_ATTENTE';

  readonly statutOptions: { value: 'ALL' | StatutBail; label: string }[] = [
    { value: 'ALL', label: 'Tous les statuts' },
    { value: 'EN_ATTENTE_VALIDATION_AGENT', label: 'À valider (mandat)' },
    { value: 'ACTIF', label: 'Actif' },
    { value: 'EN_ATTENTE', label: 'En attente propriétaire' },
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
      const pool =
        `${c.numContrat} ${c.bien.reference} ${c.locataire.firstName} ${c.locataire.lastName}`.toLowerCase();
      return pool.includes(q);
    });
  });

  readonly aValiderCount = computed(
    () =>
      this.rows().filter((c) => c.statut === 'EN_ATTENTE_VALIDATION_AGENT' || c.statut === 'EN_ATTENTE').length
  );

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .listContrats(null)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (rows) => this.rows.set(rows),
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Chargement des baux impossible.'))
      });
  }

  statutLabel(s: StatutBail): string {
    return (
      {
        ACTIF: 'Actif',
        RESILIE: 'Résilié',
        TERMINE: 'Terminé',
        EN_ATTENTE: 'En attente (propriétaire)',
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

  /** Demande à traiter par l’agent (mandat / file agent), y compris certains EN_ATTENTE sous mandat. */
  canValiderDemande(c: ContratResponse): boolean {
    return c.statut === 'EN_ATTENTE_VALIDATION_AGENT' || c.statut === 'EN_ATTENTE';
  }

  canModifierContrat(c: ContratResponse): boolean {
    return c.statut !== 'REFUSE' && c.statut !== 'RESILIE' && c.statut !== 'TERMINE';
  }

  /** Comme l’espace propriétaire : pas de changement de statut pour les baux clos. */
  statutChangeDisabled(c: ContratResponse): boolean {
    return c.statut === 'REFUSE' || c.statut === 'RESILIE' || c.statut === 'TERMINE';
  }

  /**
   * Inclut le statut courant pour que le select soit cohérent.
   * File agent : Actif / Refusé ; sinon mêmes choix que le propriétaire (hors EN_ATTENTE_VALIDATION_AGENT).
   */
  statutOptionsPourEdition(c: ContratResponse): { value: StatutBail; label: string }[] {
    if (c.statut === 'EN_ATTENTE_VALIDATION_AGENT') {
      return [
        { value: 'EN_ATTENTE_VALIDATION_AGENT', label: 'En attente (agent)' },
        { value: 'ACTIF', label: 'Actif' },
        { value: 'REFUSE', label: 'Refusé' }
      ];
    }
    if (c.statut === 'EN_ATTENTE') {
      return [
        { value: 'EN_ATTENTE', label: 'En attente (propriétaire)' },
        { value: 'ACTIF', label: 'Actif' },
        { value: 'REFUSE', label: 'Refusé' }
      ];
    }
    return [
      { value: 'EN_ATTENTE', label: 'En attente (propriétaire)' },
      { value: 'ACTIF', label: 'Actif' },
      { value: 'RESILIE', label: 'Résilié' },
      { value: 'TERMINE', label: 'Terminé' }
    ];
  }

  formatMoney(n: number | undefined | null): string {
    if (n == null || Number.isNaN(n)) return '—';
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(n);
  }

  openView(c: ContratResponse): void {
    this.viewContrat.set(null);
    this.viewLoading.set(true);
    this.api
      .getContrat(c.id)
      .pipe(finalize(() => this.viewLoading.set(false)))
      .subscribe({
        next: (d) => this.viewContrat.set(d),
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Impossible de charger le bail.');
          await this.alert.error('Erreur', msg);
        }
      });
  }

  closeView(): void {
    this.viewContrat.set(null);
  }

  openEdit(c: ContratResponse): void {
    this.editContrat.set(c);
    this.editDateDebut = c.dateDebut ?? '';
    this.editDateFin = c.dateFin ?? '';
    this.editLoyerHC = c.loyerHC ?? null;
    this.editCharges = c.charges ?? null;
    this.editDateSignature = c.dateSignature ?? '';
    this.editDocumentUrl = c.documentUrl ?? '';
    this.editStatut = c.statut;
  }

  closeEdit(): void {
    this.editContrat.set(null);
  }

  saveEdit(): void {
    const c = this.editContrat();
    if (!c) return;
    this.editSaving.set(true);
    this.error.set(null);
    this.api
      .updateContrat(c.id, {
        dateDebut: this.editDateDebut.trim() || null,
        dateFin: this.editDateFin.trim() || null,
        loyerHC: this.editLoyerHC,
        charges: this.editCharges,
        dateSignature: this.editDateSignature.trim() || null,
        documentUrl: this.editDocumentUrl.trim() || null
      })
      .pipe(finalize(() => this.editSaving.set(false)))
      .subscribe({
        next: async () => {
          await this.alert.success('Enregistré', 'Le bail a été mis à jour.');
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

  async saveStatutFromEdit(): Promise<void> {
    const c = this.editContrat();
    if (!c || this.statutChangeDisabled(c)) return;
    if (this.editStatut === c.statut) return;
    const ok = await this.alert.confirm({
      title: 'Changer le statut du bail ?',
      text: `Nouveau statut : ${this.statutLabel(this.editStatut)}`,
      confirmButtonText: 'Confirmer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.statutSaving.set(true);
    this.error.set(null);
    this.api
      .updateContratStatut(c.id, this.editStatut)
      .pipe(finalize(() => this.statutSaving.set(false)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', 'Statut du bail mis à jour.');
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

  async accepterDemande(c: ContratResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Accepter cette demande de location ?',
      text: `${c.numContrat} — ${c.bien.reference}`,
      confirmButtonText: 'Activer le bail',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;
    this.patchStatut(c.id, 'ACTIF');
  }

  async refuserDemande(c: ContratResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Refuser cette demande ?',
      text: 'Le bien redeviendra disponible pour d’autres candidatures.',
      confirmButtonText: 'Refuser',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;
    this.patchStatut(c.id, 'REFUSE');
  }

  private patchStatut(id: string, statut: StatutBail): void {
    this.actionId.set(id);
    this.error.set(null);
    this.api
      .updateContratStatut(id, statut)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', 'Statut du bail mis à jour.');
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
