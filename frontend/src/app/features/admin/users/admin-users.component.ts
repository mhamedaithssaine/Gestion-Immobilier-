import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { UtilisateurResponse } from '../models/admin-api.types';
import { AdminUserService } from '../services/admin-user.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements OnInit {
  private readonly usersApi = inject(AdminUserService);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<UtilisateurResponse[]>([]);

  filterEnabled = signal<'all' | 'yes' | 'no'>('all');
  filterSearch = signal('');

  // --- Pagination serveur (Spring Data Page) ---
  // `pageIndex` est en base 0 (conforme à l’API backend).
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly pageSizeOptions = [5, 10, 20, 50] as const;

  readonly totalElements = signal(0);
  readonly serverTotalPages = signal(1);

  // --- Create ---
  readonly createOpen = signal(false);
  readonly creating = signal(false);
  createForm = {
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    roles: ['ROLE_CLIENT'] as string[]
  };

  // --- Edit roles ---
  readonly editRolesUserId = signal<string | null>(null);
  readonly editRoles = signal<string[]>([]);
  readonly availableRoles = ['ROLE_CLIENT', 'ROLE_PROPRIETAIRE', 'ROLE_AGENT', 'ROLE_ADMIN'] as const;

  // --- Edit user profile ---
  readonly editUserId = signal<string | null>(null);
  editForm = {
    firstName: '',
    lastName: '',
    username: '',
    email: ''
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

  readonly totalPages = computed(() => Math.max(1, this.serverTotalPages()));

  readonly rangeLabel = computed(() => {
    const total = this.totalElements();
    if (total === 0) return '0';
    const start = this.pageIndex() * this.pageSize() + 1;
    const end = Math.min(total, start + this.rows().length - 1);
    return `${start}-${end} / ${total}`;
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loadPage(true);
  }

  private loadPage(sync: boolean): void {
    this.loading.set(true);
    this.error.set(null);
    this.usersApi
      .getUsersPaged(sync, this.pageIndex(), this.pageSize())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (page) => {
          this.rows.set(page.content);
          this.totalElements.set(page.totalElements);
          this.serverTotalPages.set(page.totalPages);
        },
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Impossible de charger les utilisateurs (paged).'))
      });
  }

  rolesLabel(u: UtilisateurResponse): string {
    return u.roles?.join(', ') ?? '—';
  }

  prevPage(): void {
    const next = Math.max(0, this.pageIndex() - 1);
    if (next === this.pageIndex()) return;
    this.pageIndex.set(next);
    this.loadPage(false);
  }

  nextPage(): void {
    const next = Math.min(this.totalPages() - 1, this.pageIndex() + 1);
    if (next === this.pageIndex()) return;
    this.pageIndex.set(next);
    this.loadPage(false);
  }

  goToPage(p: number): void {
    const target = Math.min(this.totalPages() - 1, Math.max(0, Math.floor(p)));
    if (target === this.pageIndex()) return;
    this.pageIndex.set(target);
    this.loadPage(false);
  }

  changePageSize(size: number): void {
    const s = Number(size);
    this.pageSize.set(s);
    this.pageIndex.set(0);
    this.loadPage(false);
  }

  async setEnabled(u: UtilisateurResponse, enabled: boolean): Promise<void> {
    const ok = await this.alert.confirm({
      title: enabled ? 'Activer cet utilisateur ?' : 'Désactiver cet utilisateur ?',
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
          await this.alert.success('Succès', enabled ? 'Compte activé.' : 'Compte désactivé.');
          this.reload();
        },
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Mise à jour impossible.'))
      });
  }

  toggleCreate(): void {
    this.createOpen.set(!this.createOpen());
    if (this.createOpen()) {
      this.error.set(null);
    }
  }

  async createUser(): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Créer cet utilisateur ?',
      text: `${this.createForm.username} (${this.createForm.email})`,
      confirmButtonText: 'Créer',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;

    this.creating.set(true);
    this.usersApi
      .createUser({ ...this.createForm })
      .pipe(finalize(() => this.creating.set(false)))
      .subscribe({
        next: async () => {
          this.createOpen.set(false);
          await this.alert.success('Utilisateur créé', 'Le compte a été créé.');
          this.createForm = {
            firstName: '',
            lastName: '',
            username: '',
            email: '',
            password: '',
            roles: ['ROLE_CLIENT']
          };
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, "Création impossible.");
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  startEditRoles(u: UtilisateurResponse): void {
    this.editRolesUserId.set(u.id);
    this.editRoles.set([...(u.roles ?? [])]);
  }

  cancelEditRoles(): void {
    this.editRolesUserId.set(null);
    this.editRoles.set([]);
  }

  startEditUser(u: UtilisateurResponse): void {
    this.editUserId.set(u.id);
    this.editForm = {
      firstName: u.firstName ?? '',
      lastName: u.lastName ?? '',
      username: u.username ?? '',
      email: u.email ?? ''
    };
  }

  cancelEditUser(): void {
    this.editUserId.set(null);
    this.editForm = { firstName: '', lastName: '', username: '', email: '' };
  }

  async saveUser(u: UtilisateurResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Modifier cet utilisateur ?',
      text: `${u.username} → ${this.editForm.username}`,
      confirmButtonText: 'Enregistrer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(u.id);
    this.usersApi
      .updateUser(u.id, { ...this.editForm })
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          this.cancelEditUser();
          await this.alert.success('Succès', 'Utilisateur modifié.');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Modification impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  toggleRole(role: string): void {
    const current = this.editRoles();
    if (current.includes(role)) {
      this.editRoles.set(current.filter((r) => r !== role));
    } else {
      this.editRoles.set([...current, role]);
    }
  }

  async saveRoles(u: UtilisateurResponse): Promise<void> {
    const roles = this.editRoles();
    if (!roles.length) {
      await this.alert.error('Erreur', 'Choisis au moins un rôle.');
      return;
    }

    const ok = await this.alert.confirm({
      title: 'Mettre à jour les rôles ?',
      text: `${u.username} → ${roles.join(', ')}`,
      confirmButtonText: 'Enregistrer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(u.id);
    this.usersApi
      .assignRoles(u.id, roles)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          this.cancelEditRoles();
          await this.alert.success('Succès', 'Rôles mis à jour.');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Mise à jour impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  async deleteUser(u: UtilisateurResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Supprimer cet utilisateur ?',
      text: `${u.username} (${u.email}) — action irréversible`,
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
        next: async () => {
          await this.alert.success('Supprimé', 'Utilisateur supprimé.');
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
