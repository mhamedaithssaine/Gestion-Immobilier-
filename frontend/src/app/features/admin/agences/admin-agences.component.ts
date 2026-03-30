import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { AgenceResponse, StatutAgence } from '../models/admin-api.types';
import { AdminAgenceService } from '../services/admin-agence.service';

@Component({
  selector: 'app-admin-agences',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-agences.component.html',
  styleUrl: './admin-agences.component.scss'
})
export class AdminAgencesComponent implements OnInit {
  private readonly agencesApi = inject(AdminAgenceService);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<AgenceResponse[]>([]);
  readonly selected = signal<AgenceResponse | null>(null);
  readonly editing = signal<AgenceResponse | null>(null);
  readonly createOpen = signal(false);
  readonly creating = signal(false);
  readonly saving = signal(false);

  createForm = {
    nom: '',
    email: '',
    telephone: '',
    adresse: '',
    ville: '',
    agentUsername: '',
    agentEmail: '',
    agentFirstName: '',
    agentLastName: '',
    agentPassword: ''
  };

  editForm = {
    nom: '',
    email: '',
    telephone: '',
    adresse: '',
    ville: ''
  };

  filterStatut = signal<'ALL' | StatutAgence>('ALL');
  filterSearch = signal('');

  readonly statutOptions: { value: 'ALL' | StatutAgence; label: string }[] = [
    { value: 'ALL', label: 'Tous les statuts' },
    { value: 'PENDING', label: 'En attente' },
    { value: 'ACTIVE', label: 'Actives' },
    { value: 'REJECTED', label: 'Rejetées' },
    { value: 'SUSPENDED', label: 'Suspendues' }
  ];

  readonly filteredRows = computed(() => {
    const s = this.filterStatut();
    const q = this.filterSearch().trim().toLowerCase();
    return this.rows().filter((a) => {
      if (s !== 'ALL' && a.statut !== s) return false;
      if (!q) return true;
      return (
        a.nom.toLowerCase().includes(q) ||
        a.email.toLowerCase().includes(q) ||
        a.ville.toLowerCase().includes(q)
      );
    });
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.agencesApi
      .listerToutes()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (list) => this.rows.set(list),
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Impossible de charger les agences.'))
      });
  }

  labelStatut(s: StatutAgence): string {
    const m: Record<StatutAgence, string> = {
      PENDING: 'En attente',
      ACTIVE: 'Active',
      REJECTED: 'Rejetée',
      SUSPENDED: 'Suspendue'
    };
    return m[s] ?? s;
  }

  async approve(a: AgenceResponse): Promise<void> {
    await this.patchWithConfirm(
      a,
      a.statut === 'SUSPENDED' ? 'Réactiver cette agence ?' : 'Approuver cette agence ?',
      a.statut === 'SUSPENDED' ? 'Réactiver' : 'Approuver',
      () => this.agencesApi.approuver(a.id),
      'Statut agence mis à jour: Active.'
    );
  }

  async reject(a: AgenceResponse): Promise<void> {
    await this.patchWithConfirm(
      a,
      'Rejeter cette agence ?',
      'Rejeter',
      () => this.agencesApi.rejeter(a.id),
      'Agence rejetée.'
    );
  }

  async suspend(a: AgenceResponse): Promise<void> {
    await this.patchWithConfirm(
      a,
      'Suspendre cette agence ?',
      'Suspendre',
      () => this.agencesApi.suspendre(a.id),
      'Agence suspendue.'
    );
  }

  canApprove(s: StatutAgence): boolean {
    return s === 'PENDING' || s === 'SUSPENDED' || s === 'REJECTED';
  }

  canReject(s: StatutAgence): boolean {
    return s === 'PENDING' || s === 'ACTIVE' || s === 'SUSPENDED';
  }

  canSuspend(s: StatutAgence): boolean {
    return s === 'ACTIVE';
  }

  openDetail(a: AgenceResponse): void {
    this.selected.set(a);
  }

  closeDetail(): void {
    this.selected.set(null);
  }

  openEdit(a: AgenceResponse): void {
    this.editing.set(a);
    this.editForm = {
      nom: a.nom ?? '',
      email: a.email ?? '',
      telephone: a.telephone ?? '',
      adresse: a.adresse ?? '',
      ville: a.ville ?? ''
    };
  }

  closeEdit(): void {
    this.editing.set(null);
  }

  toggleCreate(): void {
    this.createOpen.update((v) => !v);
    if (this.createOpen()) return;
    this.resetCreateForm();
  }

  createAgence(): void {
    this.creating.set(true);
    this.error.set(null);
    this.agencesApi
      .create({
        nom: this.createForm.nom,
        email: this.createForm.email,
        telephone: this.createForm.telephone || undefined,
        adresse: this.createForm.adresse || undefined,
        ville: this.createForm.ville || undefined,
        agentUsername: this.createForm.agentUsername,
        agentEmail: this.createForm.agentEmail,
        agentFirstName: this.createForm.agentFirstName,
        agentLastName: this.createForm.agentLastName,
        agentPassword: this.createForm.agentPassword
      })
      .pipe(finalize(() => this.creating.set(false)))
      .subscribe({
        next: async () => {
          await this.alert.success('Créée', "Demande d'inscription agence créée avec succès.");
          this.resetCreateForm();
          this.createOpen.set(false);
          this.reload();
        },
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, "Création d'agence impossible."))
      });
  }

  async saveEdit(id: string): Promise<void> {
    const a = this.editing();
    const ok = await this.alert.confirm({
      title: 'Enregistrer les modifications ?',
      text: a ? `${a.nom} (${this.editForm.email})` : 'Agence',
      confirmButtonText: 'Enregistrer',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;

    this.saving.set(true);
    this.error.set(null);
    this.agencesApi
      .update(id, { ...this.editForm })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', 'Agence modifiée avec succès.');
          this.closeEdit();
          this.reload();
        },
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Modification impossible.'))
      });
  }

  async deleteAgence(a: AgenceResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Supprimer cette agence ?',
      text: `${a.nom} (${a.email})`,
      confirmButtonText: 'Supprimer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;
    this.actionId.set(a.id);
    this.agencesApi
      .delete(a.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Supprimée', 'Agence supprimée.');
          this.reload();
        },
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Suppression impossible.'))
      });
  }

  private async patchWithConfirm(
    agence: AgenceResponse,
    title: string,
    confirmButtonText: string,
    call: () => ReturnType<AdminAgenceService['approuver']>,
    successMessage: string
  ): Promise<void> {
    const ok = await this.alert.confirm({
      title,
      text: `${agence.nom} (${agence.email})`,
      confirmButtonText,
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(agence.id);
    call()
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', successMessage);
          this.reload();
        },
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Action impossible.'))
      });
  }

  private resetCreateForm(): void {
    this.createForm = {
      nom: '',
      email: '',
      telephone: '',
      adresse: '',
      ville: '',
      agentUsername: '',
      agentEmail: '',
      agentFirstName: '',
      agentLastName: '',
      agentPassword: ''
    };
  }
}
