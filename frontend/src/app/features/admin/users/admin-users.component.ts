import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { concatMap, finalize } from 'rxjs/operators';
import { AlertService } from '../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import {
  LocataireResponse,
  ProprietaireResponse,
  StatutDossier,
  UtilisateurResponse
} from '../models/admin-api.types';
import { AdminLocataireService } from '../services/admin-locataire.service';
import { AdminProprietaireService } from '../services/admin-proprietaire.service';
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
  private readonly proprietaireApi = inject(AdminProprietaireService);
  private readonly locataireApi = inject(AdminLocataireService);
  private readonly alert = inject(AlertService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  /** Vue « tous les utilisateurs » (Keycloak / users pagés). */
  readonly userRows = signal<UtilisateurResponse[]>([]);
  /** Données brutes pour pagination client (vues locataire / propriétaire). */
  readonly proprietaireAll = signal<ProprietaireResponse[]>([]);
  readonly locataireAll = signal<LocataireResponse[]>([]);

  readonly currentView = signal<'locataire' | 'proprietaire' | 'all'>('all');

  filterEnabled = signal<'all' | 'yes' | 'no'>('all');
  filterSearch = signal('');

  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly pageSizeOptions = [5, 10, 20, 50] as const;

  readonly totalElements = signal(0);
  readonly serverTotalPages = signal(1);

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

  createProprioForm = {
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    rib: '',
    adresseContact: ''
  };

  createLocForm = {
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    budgetMax: null as number | null,
    statutDossier: 'EN_ATTENTE' as StatutDossier
  };

  readonly statutDossierOptions: StatutDossier[] = ['EN_ATTENTE', 'VALIDE', 'REFUSE'];

  readonly editRolesUserId = signal<string | null>(null);
  readonly editRoles = signal<string[]>([]);
  readonly availableRoles = ['ROLE_CLIENT', 'ROLE_PROPRIETAIRE', 'ROLE_AGENT', 'ROLE_ADMIN'] as const;
  readonly scopedRoles = computed(() => {
    if (this.currentView() === 'locataire') return ['ROLE_CLIENT'] as const;
    if (this.currentView() === 'proprietaire') return ['ROLE_PROPRIETAIRE'] as const;
    return this.availableRoles;
  });
  readonly isScopedView = computed(() => this.currentView() !== 'all');
  readonly pageTitle = computed(() => {
    if (this.currentView() === 'locataire') return 'Locataires';
    if (this.currentView() === 'proprietaire') return 'Propriétaires';
    return 'Utilisateurs';
  });
  readonly pageIntro = computed(() => {
    if (this.currentView() === 'locataire') {
      return 'Comptes locataires : identité (Keycloak + table utilisateurs) et dossier (budget, statut dans la table clients).';
    }
    if (this.currentView() === 'proprietaire') {
      return 'Comptes propriétaires : identité (Keycloak + utilisateurs) et fiche métier RIB / adresse (table proprietaires, héritage JOINED).';
    }
    return 'Utilisateurs synchronisés depuis Keycloak (GET /api/admin/users?sync=true). Filtrez et activez ou désactivez les comptes.';
  });

  readonly editUserId = signal<string | null>(null);
  editForm = {
    firstName: '',
    lastName: '',
    username: '',
    email: ''
  };

  editProprioForm = { rib: '', adresseContact: '' };
  editLocForm = { budgetMax: null as number | null, statutDossier: 'EN_ATTENTE' as StatutDossier };

  readonly proprietaireFiltered = computed(() => {
    const fe = this.filterEnabled();
    const q = this.filterSearch().trim().toLowerCase();
    return this.proprietaireAll().filter((p) => {
      if (fe === 'yes' && !p.enabled) return false;
      if (fe === 'no' && p.enabled) return false;
      if (!q) return true;
      const pool = `${p.username} ${p.email} ${p.firstName} ${p.lastName} ${p.rib ?? ''} ${p.adresseContact ?? ''}`.toLowerCase();
      return pool.includes(q);
    });
  });

  readonly locataireFiltered = computed(() => {
    const fe = this.filterEnabled();
    const q = this.filterSearch().trim().toLowerCase();
    return this.locataireAll().filter((c) => {
      if (fe === 'yes' && !c.enabled) return false;
      if (fe === 'no' && c.enabled) return false;
      if (!q) return true;
      const pool = `${c.username} ${c.email} ${c.firstName} ${c.lastName} ${c.statutDossier ?? ''}`.toLowerCase();
      return pool.includes(q);
    });
  });

  readonly displayedProprietaires = computed(() => {
    const list = this.proprietaireFiltered();
    const start = this.pageIndex() * this.pageSize();
    return list.slice(start, start + this.pageSize());
  });

  readonly displayedLocataires = computed(() => {
    const list = this.locataireFiltered();
    const start = this.pageIndex() * this.pageSize();
    return list.slice(start, start + this.pageSize());
  });

  readonly filteredUserRows = computed(() => {
    const fe = this.filterEnabled();
    const q = this.filterSearch().trim().toLowerCase();
    return this.userRows().filter((u) => {
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
    let onPage = 0;
    if (this.currentView() === 'proprietaire') onPage = this.displayedProprietaires().length;
    else if (this.currentView() === 'locataire') onPage = this.displayedLocataires().length;
    else onPage = this.filteredUserRows().length;
    if (onPage === 0) return `0 / ${total}`;
    const end = Math.min(total, start + onPage - 1);
    return `${start}-${end} / ${total}`;
  });

  ngOnInit(): void {
    this.updateCurrentView();
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateCurrentView();
        this.pageIndex.set(0);
        this.cancelEditUser();
        this.cancelEditRoles();
        this.reload();
      });
    this.reload();
  }

  reload(): void {
    this.loadPage(true);
  }

  private loadPage(sync: boolean): void {
    this.loading.set(true);
    this.error.set(null);
    const view = this.currentView();

    if (view === 'proprietaire') {
      this.proprietaireApi
        .list()
        .pipe(finalize(() => this.loading.set(false)))
        .subscribe({
          next: (list) => {
            this.proprietaireAll.set(list);
            this.applyClientPaginationTotals(this.proprietaireFiltered().length);
          },
          error: (err: unknown) =>
            this.error.set(getApiErrorMessage(err, 'Impossible de charger les propriétaires.'))
        });
      return;
    }

    if (view === 'locataire') {
      this.locataireApi
        .list()
        .pipe(finalize(() => this.loading.set(false)))
        .subscribe({
          next: (list) => {
            this.locataireAll.set(list);
            this.applyClientPaginationTotals(this.locataireFiltered().length);
          },
          error: (err: unknown) =>
            this.error.set(getApiErrorMessage(err, 'Impossible de charger les locataires.'))
        });
      return;
    }

    this.usersApi
      .getUsersPaged(sync, this.pageIndex(), this.pageSize())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (page) => {
          this.userRows.set(page.content);
          this.totalElements.set(page.totalElements);
          this.serverTotalPages.set(page.totalPages);
        },
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Impossible de charger les utilisateurs (paged).'))
      });
  }

  private applyClientPaginationTotals(filteredCount: number): void {
    this.totalElements.set(filteredCount);
    const pages = Math.max(1, Math.ceil(filteredCount / this.pageSize()) || 1);
    this.serverTotalPages.set(pages);
    if (this.pageIndex() >= pages) {
      this.pageIndex.set(Math.max(0, pages - 1));
    }
  }

  /** Recalcule les totaux après filtre (search / enabled) sur listes client. */
  private refreshScopedPagination(): void {
    if (this.currentView() === 'proprietaire') {
      this.applyClientPaginationTotals(this.proprietaireFiltered().length);
    } else if (this.currentView() === 'locataire') {
      this.applyClientPaginationTotals(this.locataireFiltered().length);
    }
  }

  prevPage(): void {
    const next = Math.max(0, this.pageIndex() - 1);
    if (next === this.pageIndex()) return;
    this.pageIndex.set(next);
    if (this.currentView() === 'all') this.loadPage(false);
    else this.refreshScopedPagination();
  }

  nextPage(): void {
    const next = Math.min(this.totalPages() - 1, this.pageIndex() + 1);
    if (next === this.pageIndex()) return;
    this.pageIndex.set(next);
    if (this.currentView() === 'all') this.loadPage(false);
    else this.refreshScopedPagination();
  }

  changePageSize(size: number): void {
    const s = Number(size);
    this.pageSize.set(s);
    this.pageIndex.set(0);
    if (this.currentView() === 'all') this.loadPage(false);
    else {
      this.refreshScopedPagination();
    }
  }

  onFilterChange(): void {
    this.pageIndex.set(0);
    if (this.currentView() !== 'all') this.refreshScopedPagination();
  }

  rolesLabel(u: UtilisateurResponse): string {
    return u.roles?.join(', ') ?? '—';
  }

  async setEnabled(
    id: string,
    username: string,
    email: string,
    enabled: boolean
  ): Promise<void> {
    const ok = await this.alert.confirm({
      title: enabled ? 'Activer ce compte ?' : 'Désactiver ce compte ?',
      text: `${username} (${email})`,
      confirmButtonText: enabled ? 'Activer' : 'Désactiver',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(id);
    this.usersApi
      .setEnabled(id, enabled)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Succès', enabled ? 'Compte activé.' : 'Compte désactivé.');
          this.reload();
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Mise à jour impossible.'))
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
          const msg = getApiErrorMessage(err, 'Création impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  async createProprietaire(): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Créer ce propriétaire ?',
      text: `${this.createProprioForm.username} (${this.createProprioForm.email})`,
      confirmButtonText: 'Créer',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;

    this.creating.set(true);
    this.proprietaireApi
      .create({
        username: this.createProprioForm.username,
        email: this.createProprioForm.email,
        firstName: this.createProprioForm.firstName,
        lastName: this.createProprioForm.lastName,
        password: this.createProprioForm.password,
        rib: this.createProprioForm.rib || undefined,
        adresseContact: this.createProprioForm.adresseContact || undefined
      })
      .pipe(finalize(() => this.creating.set(false)))
      .subscribe({
        next: async () => {
          this.createOpen.set(false);
          await this.alert.success('Propriétaire créé', 'Compte et fiche enregistrés.');
          this.createProprioForm = {
            firstName: '',
            lastName: '',
            username: '',
            email: '',
            password: '',
            rib: '',
            adresseContact: ''
          };
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Création impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  async createLocataire(): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Créer ce locataire ?',
      text: `${this.createLocForm.username} (${this.createLocForm.email})`,
      confirmButtonText: 'Créer',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;

    this.creating.set(true);
    this.locataireApi
      .create({
        username: this.createLocForm.username,
        email: this.createLocForm.email,
        firstName: this.createLocForm.firstName,
        lastName: this.createLocForm.lastName,
        password: this.createLocForm.password,
        budgetMax: this.createLocForm.budgetMax,
        statutDossier: this.createLocForm.statutDossier
      })
      .pipe(finalize(() => this.creating.set(false)))
      .subscribe({
        next: async () => {
          this.createOpen.set(false);
          await this.alert.success('Locataire créé', 'Compte et dossier enregistrés.');
          this.createLocForm = {
            firstName: '',
            lastName: '',
            username: '',
            email: '',
            password: '',
            budgetMax: null,
            statutDossier: 'EN_ATTENTE'
          };
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Création impossible.');
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

  startEditProprietaire(p: ProprietaireResponse): void {
    this.editUserId.set(p.id);
    this.editForm = {
      firstName: p.firstName ?? '',
      lastName: p.lastName ?? '',
      username: p.username ?? '',
      email: p.email ?? ''
    };
    this.editProprioForm = {
      rib: p.rib ?? '',
      adresseContact: p.adresseContact ?? ''
    };
  }

  startEditLocataire(c: LocataireResponse): void {
    this.editUserId.set(c.id);
    this.editForm = {
      firstName: c.firstName ?? '',
      lastName: c.lastName ?? '',
      username: c.username ?? '',
      email: c.email ?? ''
    };
    this.editLocForm = {
      budgetMax: c.budgetMax,
      statutDossier: c.statutDossier ?? 'EN_ATTENTE'
    };
  }

  cancelEditUser(): void {
    this.editUserId.set(null);
    this.editForm = { firstName: '', lastName: '', username: '', email: '' };
    this.editProprioForm = { rib: '', adresseContact: '' };
    this.editLocForm = { budgetMax: null, statutDossier: 'EN_ATTENTE' };
  }

  async saveUser(u: UtilisateurResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Modifier cet utilisateur ?',
      text: `${u.username} — ${this.editForm.email}`,
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

  async saveProprietaire(p: ProprietaireResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Enregistrer les modifications ?',
      text: `${p.username}`,
      confirmButtonText: 'Enregistrer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(p.id);
    // Séquentiel obligatoire : même ligne JPA (id + @Version) pour Utilisateur / Proprietaire — en parallèle → ObjectOptimisticLockingFailureException
    this.usersApi
      .updateUser(p.id, { ...this.editForm })
      .pipe(
        concatMap(() =>
          this.proprietaireApi.update(p.id, {
            rib: this.editProprioForm.rib || undefined,
            adresseContact: this.editProprioForm.adresseContact || undefined
          })
        ),
        finalize(() => this.actionId.set(null))
      )
      .subscribe({
        next: async () => {
          this.cancelEditUser();
          await this.alert.success('Succès', 'Propriétaire mis à jour.');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Modification impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  async saveLocataire(c: LocataireResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Enregistrer les modifications ?',
      text: `${c.username}`,
      confirmButtonText: 'Enregistrer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(c.id);
    this.usersApi
      .updateUser(c.id, { ...this.editForm })
      .pipe(
        concatMap(() =>
          this.locataireApi.update(c.id, {
            budgetMax: this.editLocForm.budgetMax,
            statutDossier: this.editLocForm.statutDossier
          })
        ),
        finalize(() => this.actionId.set(null))
      )
      .subscribe({
        next: async () => {
          this.cancelEditUser();
          await this.alert.success('Succès', 'Locataire mis à jour.');
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
    if (this.isScopedView()) {
      return;
    }
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

  async deleteProprietaire(p: ProprietaireResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Supprimer ce propriétaire ?',
      text: `${p.username} (${p.email}) — action irréversible`,
      confirmButtonText: 'Supprimer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(p.id);
    this.proprietaireApi
      .delete(p.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Supprimé', 'Propriétaire supprimé.');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Suppression impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  async deleteLocataire(c: LocataireResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Supprimer ce locataire ?',
      text: `${c.username} (${c.email}) — action irréversible`,
      confirmButtonText: 'Supprimer',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.actionId.set(c.id);
    this.locataireApi
      .delete(c.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Supprimé', 'Locataire supprimé.');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Suppression impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  private updateCurrentView(): void {
    const path = this.route.snapshot.routeConfig?.path ?? '';
    if (path === 'locataire') {
      this.currentView.set('locataire');
      return;
    }
    if (path === 'proprietaire') {
      this.currentView.set('proprietaire');
      return;
    }
    this.currentView.set('all');
  }
}
