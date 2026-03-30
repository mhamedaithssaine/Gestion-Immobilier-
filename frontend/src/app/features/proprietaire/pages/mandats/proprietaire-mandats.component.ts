import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../../../core/services/alert.service';
import { getApiErrorMessage } from '../../../../core/http/error-message.util';
import { filenameFromContentDisposition, triggerBlobDownload } from '../../../../core/http/download-blob.util';
import { MandatResponse, StatutMandat } from '../../../admin/models/admin-api.types';
import { ProprietaireEspaceService } from '../../services/proprietaire-espace.service';

@Component({
  selector: 'app-proprietaire-mandats',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './proprietaire-mandats.component.html',
  styleUrl: './proprietaire-mandats.component.scss'
})
export class ProprietaireMandatsComponent implements OnInit {
  private readonly api = inject(ProprietaireEspaceService);
  private readonly alert = inject(AlertService);

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
      .listMandats()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (rows) => this.rows.set(rows),
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Chargement des mandats impossible.'))
      });
  }

  statutLabel(s: StatutMandat): string {
    return (
      {
        ACTIF: 'Actif',
        RESILIE: 'Résilié',
        TERMINE: 'Terminé',
        EN_ATTENTE: 'En attente',
        EN_ATTENTE_RESILIATION: 'Résiliation en attente'
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
      .demanderResiliationMandat(m.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Demande envoyée', 'Votre demande a été transmise à l’administrateur.');
          this.closeDetail();
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Demande impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }
}
