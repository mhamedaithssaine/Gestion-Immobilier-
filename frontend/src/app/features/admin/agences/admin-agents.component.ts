import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { UtilisateurResponse } from '../models/admin-api.types';
import { AdminUserService } from '../services/admin-user.service';

@Component({
  selector: 'app-admin-agents',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-agents.component.html',
  styleUrl: './admin-agents.component.scss'
})
export class AdminAgentsComponent implements OnInit {
  private readonly usersApi = inject(AdminUserService);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<UtilisateurResponse[]>([]);
  readonly selected = signal<UtilisateurResponse | null>(null);
  readonly editing = signal<UtilisateurResponse | null>(null);
  readonly creating = signal(false);
  readonly saving = signal(false);
  readonly createOpen = signal(false);

  readonly filterEnabled = signal<'all' | 'yes' | 'no'>('all');
  readonly filterSearch = signal('');
  createForm = {
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    password: ''
  };
  editForm = {
    username: '',
    email: '',
    firstName: '',
    lastName: ''
  };

  readonly filteredRows = computed(() => {
    const fe = this.filterEnabled();
    const q = this.filterSearch().trim().toLowerCase();
    return this.rows().filter((u) => {
      if (fe === 'yes' && !u.enabled) return false;
      if (fe === 'no' && u.enabled) return false;
      if (!q) return true;
      const pool = `${u.username} ${u.email} ${u.firstName} ${u.lastName}`.toLowerCase();
      return pool.includes(q);
    });
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.usersApi
      .listUsers(true)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (users) => this.rows.set(users.filter((u) => (u.roles ?? []).includes('ROLE_AGENT'))),
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Impossible de charger les agents.'))
      });
  }

  async setEnabled(u: UtilisateurResponse, enabled: boolean): Promise<void> {
    const ok = await this.alert.confirm({
      title: enabled ? 'Activer cet agent ?' : 'Désactiver cet agent ?',
      text: `${u.username} (${u.email})`,
      confirmButtonText: enabled ? 'Activer' : 'Désactiver',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(u.id);
    this.usersApi
      .setEnabled(u.id, enabled)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', enabled ? 'Agent activé.' : 'Agent désactivé.');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Mise à jour impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  openDetail(u: UtilisateurResponse): void {
    this.selected.set(u);
  }

  closeDetail(): void {
    this.selected.set(null);
  }

  toggleCreate(): void {
    this.createOpen.set(!this.createOpen());
  }

  async createAgent(): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Créer ce compte agent ?',
      text: `${this.createForm.username} (${this.createForm.email})`,
      confirmButtonText: 'Créer',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;
    this.creating.set(true);
    this.error.set(null);
    this.usersApi
      .createUser({
        username: this.createForm.username,
        email: this.createForm.email,
        firstName: this.createForm.firstName,
        lastName: this.createForm.lastName,
        password: this.createForm.password,
        roles: ['ROLE_AGENT']
      })
      .pipe(finalize(() => this.creating.set(false)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', 'Agent créé.');
          this.createForm = { username: '', email: '', firstName: '', lastName: '', password: '' };
          this.createOpen.set(false);
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Création impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  startEdit(u: UtilisateurResponse): void {
    this.editing.set(u);
    this.editForm = {
      username: u.username ?? '',
      email: u.email ?? '',
      firstName: u.firstName ?? '',
      lastName: u.lastName ?? ''
    };
  }

  closeEdit(): void {
    this.editing.set(null);
  }

  async saveEdit(u: UtilisateurResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Modifier cet agent ?',
      text: `${u.username} (${this.editForm.email})`,
      confirmButtonText: 'Enregistrer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.saving.set(true);
    this.error.set(null);
    this.usersApi
      .updateUser(u.id, { ...this.editForm })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.closeEdit();
          this.reload();
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Modification impossible.'))
      });
  }

  async deleteAgent(u: UtilisateurResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Supprimer cet agent ?',
      text: `${u.username} (${u.email})`,
      confirmButtonText: 'Supprimer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;
    this.actionId.set(u.id);
    this.usersApi
      .deleteUser(u.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: () => this.reload(),
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Suppression impossible.'))
      });
  }
}

