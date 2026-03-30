import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { AlertService } from '../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { filenameFromContentDisposition, triggerBlobDownload } from '../../../core/http/download-blob.util';
import { BienResponse, MandatResponse, StatutMandat, UtilisateurResponse } from '../models/admin-api.types';
import { CreateMandatPayload, MandatManagementService, UpdateMandatPayload } from '../services/mandat-management.service';

@Component({
  selector: 'app-admin-mandats',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-mandats.component.html',
  styleUrl: './admin-mandats.component.scss'
})
export class AdminMandatsComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly api = inject(MandatManagementService);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly creating = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  readonly rows = signal<MandatResponse[]>([]);
  readonly biens = signal<BienResponse[]>([]);
  readonly proprietaires = signal<UtilisateurResponse[]>([]);
  readonly agents = signal<UtilisateurResponse[]>([]);
  readonly loadingBiens = signal(false);
  readonly createOpen = signal(false);
  readonly minDate = this.toDateInput(new Date());
  selectedPdf: File | null = null;
  editSelectedPdf: File | null = null;

  readonly detail = signal<MandatResponse | null>(null);
  readonly edit = signal<MandatResponse | null>(null);
  readonly downloadingDocId = signal<string | null>(null);

  editForm: {
    dateDebut: string;
    dateFin: string;
    commissionPct: number | null;
    dateSignature: string;
  } = {
    dateDebut: '',
    dateFin: '',
    commissionPct: null,
    dateSignature: ''
  };

  readonly filterStatut = signal<'ALL' | StatutMandat>('ALL');
  readonly filterQuery = signal('');

  readonly filteredRows = computed(() => {
    const s = this.filterStatut();
    const q = this.filterQuery().trim().toLowerCase();
    return this.rows().filter((m) => {
      if (s !== 'ALL' && m.statut !== s) return false;
      if (!q) return true;
      return (
        m.numMandat.toLowerCase().includes(q) ||
        m.bienReference.toLowerCase().includes(q) ||
        m.proprietaireNom.toLowerCase().includes(q) ||
        m.agentNom.toLowerCase().includes(q)
      );
    });
  });

  readonly statutOptions: { value: 'ALL' | StatutMandat; label: string }[] = [
    { value: 'ALL', label: 'Tous les statuts' },
    { value: 'ACTIF', label: 'Actif' },
    { value: 'EN_ATTENTE_RESILIATION', label: 'Résiliation en attente' },
    { value: 'EN_ATTENTE', label: 'En attente' },
    { value: 'RESILIE', label: 'Résilié' },
    { value: 'TERMINE', label: 'Terminé' }
  ];

  readonly isAdmin = computed(() => this.auth.hasRole('ROLE_ADMIN'));
  readonly isAgent = computed(() => this.auth.hasRole('ROLE_AGENT'));
  readonly canCreate = computed(() => this.auth.hasRole('ROLE_ADMIN'));

  createForm: CreateMandatPayload = {
    bienId: '',
    proprietaireId: '',
    agentId: '',
    dateDebut: '',
    dateFin: '',
    commissionPct: null,
    dateSignature: ''
  };

  ngOnInit(): void {
    this.reload();
    if (this.canCreate()) this.loadCreateDependencies();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .listMandats({ statut: this.filterStatut() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (rows) => this.rows.set(rows),
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Chargement des mandats impossible.'))
      });
  }

  toggleCreate(): void {
    this.createOpen.update((v) => !v);
    if (!this.createOpen()) {
      this.selectedPdf = null;
      this.biens.set([]);
    }
  }

  statutLabel(s: StatutMandat): string {
    return (
      {
        ACTIF: 'Actif',
        EN_ATTENTE: 'En attente',
        EN_ATTENTE_RESILIATION: 'Résiliation en attente',
        RESILIE: 'Résilié',
        TERMINE: 'Terminé'
      }[s] ?? s
    );
  }

  openDetail(m: MandatResponse): void {
    this.detail.set(m);
  }

  closeDetail(): void {
    this.detail.set(null);
  }

  openEdit(m: MandatResponse): void {
    this.edit.set(m);
    this.editSelectedPdf = null;
    this.editForm = {
      dateDebut: m.dateDebut?.slice(0, 10) ?? '',
      dateFin: m.dateFin?.slice(0, 10) ?? '',
      commissionPct: m.commissionPct ?? null,
      dateSignature: m.dateSignature ? m.dateSignature.slice(0, 10) : ''
    };
  }

  closeEdit(): void {
    this.edit.set(null);
    this.editSelectedPdf = null;
  }

  onEditPdfSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) {
      this.editSelectedPdf = null;
      return;
    }
    const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
    if (!isPdf) {
      this.editSelectedPdf = null;
      this.error.set('Le document doit être un fichier PDF.');
      void this.alert.error('Validation', 'Le document doit être un fichier PDF.');
      input.value = '';
      return;
    }
    this.editSelectedPdf = file;
  }

  saveEdit(): void {
    const m = this.edit();
    if (!m) return;
    const payload: UpdateMandatPayload = {
      dateDebut: this.editForm.dateDebut || null,
      dateFin: this.editForm.dateFin || null,
      commissionPct: this.editForm.commissionPct,
      dateSignature: this.editForm.dateSignature || null
    };
    this.actionId.set(m.id);
    const req$ =
      this.editSelectedPdf != null
        ? this.api.updateMandatMultipart(m.id, payload, this.editSelectedPdf)
        : this.api.updateMandat(m.id, payload);
    req$.pipe(finalize(() => this.actionId.set(null))).subscribe({
      next: async () => {
        await this.alert.success('Enregistré', 'Mandat mis à jour.');
        this.closeEdit();
        this.closeDetail();
        this.reload();
      },
      error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Mise à jour impossible.'))
    });
  }

  telechargerDocument(m: MandatResponse): void {
    const fallback = `${m.numMandat.replace(/[^a-zA-Z0-9_.-]/g, '_')}.pdf`;
    this.downloadingDocId.set(m.id);
    this.api
      .downloadMandatDocument(m.id)
      .pipe(finalize(() => this.downloadingDocId.set(null)))
      .subscribe({
        next: (resp) => {
          const blob = resp.body;
          if (!blob) {
            this.error.set('Fichier vide.');
            return;
          }
          const name = filenameFromContentDisposition(resp.headers.get('Content-Disposition'), fallback);
          triggerBlobDownload(blob, name);
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Téléchargement impossible.'))
      });
  }

  async createMandat(): Promise<void> {
    const validationError = this.validateCreateForm();
    if (validationError) {
      this.error.set(validationError);
      await this.alert.error('Validation', validationError);
      return;
    }

    const ok = await this.alert.confirm({
      title: 'Créer ce mandat ?',
      text: 'Le mandat sera créé en statut actif.',
      confirmButtonText: 'Créer',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;

    this.creating.set(true);
    this.error.set(null);
    this.api
      .createMandatMultipart(
        {
          ...this.createForm,
          commissionPct:
            this.createForm.commissionPct === null || this.createForm.commissionPct === undefined
              ? null
              : Number(this.createForm.commissionPct),
          dateSignature: this.createForm.dateSignature || null
        },
        this.selectedPdf
      )
      .pipe(finalize(() => this.creating.set(false)))
      .subscribe({
        next: async () => {
          await this.alert.success('Créé', 'Mandat créé avec succès.');
          this.resetCreateForm();
          this.createOpen.set(false);
          this.reload();
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Création mandat impossible.'))
      });
  }

  async resilierMandat(m: MandatResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Résilier ce mandat ?',
      text: m.numMandat,
      confirmButtonText: 'Résilier',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(m.id);
    this.api
      .resilierMandat(m.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', 'Mandat résilié.');
          this.closeDetail();
          this.reload();
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Résiliation impossible.'))
      });
  }

  async approuverResiliation(m: MandatResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Approuver la résiliation ?',
      text: m.numMandat,
      confirmButtonText: 'Approuver',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;
    this.actionId.set(m.id);
    this.api
      .approuverResiliation(m.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', 'Résiliation approuvée.');
          this.closeDetail();
          this.reload();
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Action impossible.'))
      });
  }

  async rejeterDemande(m: MandatResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Rejeter la demande ?',
      text: 'Le mandat redeviendra actif.',
      confirmButtonText: 'Rejeter',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;
    this.actionId.set(m.id);
    this.api
      .rejeterDemandeResiliation(m.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', 'Demande rejetée.');
          this.closeDetail();
          this.reload();
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Action impossible.'))
      });
  }

  onProprietaireChange(id: string): void {
    this.createForm.proprietaireId = id;
    this.createForm.bienId = '';
    this.biens.set([]);
    if (!id) return;
    this.loadingBiens.set(true);
    this.api
      .listBiensByProprietaire(id)
      .pipe(finalize(() => this.loadingBiens.set(false)))
      .subscribe({
        next: (rows) => this.biens.set(rows),
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Chargement des biens impossible.'))
      });
  }

  onDateDebutChange(value: string): void {
    this.createForm.dateDebut = value;
    if (this.createForm.dateFin && this.createForm.dateFin <= value) {
      this.createForm.dateFin = '';
    }
  }

  onPdfSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) {
      this.selectedPdf = null;
      return;
    }
    const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
    if (!isPdf) {
      this.selectedPdf = null;
      this.error.set('Le document doit être un fichier PDF.');
      void this.alert.error('Validation', 'Le document doit être un fichier PDF.');
      input.value = '';
      return;
    }
    this.selectedPdf = file;
  }

  canSubmitCreate(): boolean {
    return !this.validateCreateForm();
  }

  private loadCreateDependencies(): void {
    this.api.listProprietaires().subscribe({ next: (rows) => this.proprietaires.set(rows) });
    this.api.listAgents().subscribe({
      next: (rows) => {
        if (this.isAdmin()) {
          this.agents.set(rows);
          return;
        }
        const me = rows.find((u) => u.keycloakId === this.auth.subject());
        this.agents.set(me ? [me] : []);
        if (me) this.createForm.agentId = me.id;
      }
    });
  }

  private resetCreateForm(): void {
    this.createForm = {
      bienId: '',
      proprietaireId: '',
      agentId: '',
      dateDebut: '',
      dateFin: '',
      commissionPct: null,
      dateSignature: ''
    };
  }

  private validateCreateForm(): string | null {
    if (!this.createForm.proprietaireId) return 'Veuillez sélectionner un propriétaire.';
    if (!this.createForm.bienId) return 'Veuillez sélectionner un bien.';
    if (!this.createForm.agentId) return 'Veuillez sélectionner un agent.';
    if (!this.createForm.dateDebut) return 'La date de début est obligatoire.';
    if (!this.createForm.dateFin) return 'La date de fin est obligatoire.';

    const today = this.minDate;
    if (this.createForm.dateDebut < today) return 'La date de début ne peut pas être dans le passé.';
    if (this.createForm.dateFin <= this.createForm.dateDebut) return 'La date de fin doit être après la date de début.';
    if (this.createForm.dateSignature && this.createForm.dateSignature > today) {
      return 'La date de signature ne peut pas être dans le futur.';
    }
    return null;
  }

  private toDateInput(value: Date): string {
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
