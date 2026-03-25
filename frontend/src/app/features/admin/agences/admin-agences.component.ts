import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
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

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<AgenceResponse[]>([]);

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

  approve(id: string): void {
    this.patch(id, () => this.agencesApi.approuver(id));
  }

  reject(id: string): void {
    this.patch(id, () => this.agencesApi.rejeter(id));
  }

  suspend(id: string): void {
    this.patch(id, () => this.agencesApi.suspendre(id));
  }

  private patch(id: string, call: () => ReturnType<AdminAgenceService['approuver']>): void {
    this.actionId.set(id);
    call()
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: () => this.reload(),
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Action impossible.'))
      });
  }
}
