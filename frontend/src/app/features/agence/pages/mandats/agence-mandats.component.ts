import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../../core/http/error-message.util';
import { filenameFromContentDisposition, triggerBlobDownload } from '../../../../core/http/download-blob.util';
import { MandatResponse, StatutMandat } from '../../../admin/models/admin-api.types';
import { AgentMandatService } from '../../services/agent-mandat.service';

@Component({
  selector: 'app-agence-mandats',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './agence-mandats.component.html',
  styleUrl: './agence-mandats.component.scss'
})
export class AgenceMandatsComponent implements OnInit {
  private readonly api = inject(AgentMandatService);
  private readonly alert = inject(AlertService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<MandatResponse[]>([]);
  readonly detail = signal<MandatResponse | null>(null);
  readonly downloadingDocId = signal<string | null>(null);

  readonly filterStatut = signal<'ALL' | StatutMandat>('ALL');
  readonly filterQuery = signal('');

  readonly statutOptions: { value: 'ALL' | StatutMandat; label: string }[] = [
    { value: 'ALL', label: 'Tous les statuts' },
    { value: 'ACTIF', label: 'Actif' },
    { value: 'EN_ATTENTE_RESILIATION', label: 'Résiliation en attente' },
    { value: 'EN_ATTENTE', label: 'En attente' },
    { value: 'RESILIE', label: 'Résilié' },
    { value: 'TERMINE', label: 'Terminé' }
  ];

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

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .listMandats({ statut: this.filterStatut() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (rows) => {
          this.rows.set(rows);
          this.tryOpenMandatFromQuery(rows);
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Chargement des mandats impossible.'))
      });
  }

  /** Ouverture depuis la page Biens (`?ouvrir=<id mandat>`). */
  private tryOpenMandatFromQuery(rows: MandatResponse[]): void {
    const id = this.route.snapshot.queryParamMap.get('ouvrir');
    if (!id) return;

    const fromList = rows.find((r) => r.id === id);
    if (fromList) {
      this.openDetail(fromList);
      void this.clearOuvrirQuery();
      return;
    }

    this.api.getMandat(id).subscribe({
      next: (m) => {
        this.openDetail(m);
        void this.clearOuvrirQuery();
      },
      error: () => {
        void this.clearOuvrirQuery();
      }
    });
  }

  private clearOuvrirQuery(): Promise<boolean> {
    return this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { ouvrir: null },
      replaceUrl: true
    });
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

  telechargerDocument(m: MandatResponse): void {
    const fallback = `${m.numMandat.replace(/[^a-zA-Z0-9_.-]/g, '_')}.pdf`;
    this.downloadingDocId.set(m.id);
    this.api
      .downloadDocument(m.id)
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

  async demanderResiliation(m: MandatResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Demander la résiliation ?',
      text: `Une validation administrateur sera requise. Mandat ${m.numMandat}`,
      confirmButtonText: 'Envoyer la demande',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;

    this.actionId.set(m.id);
    this.api
      .demanderResiliation(m.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Demande envoyée', 'Votre demande a été transmise à l’administrateur.');
          this.closeDetail();
          this.reload();
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Demande impossible.'))
      });
  }
}
