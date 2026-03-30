import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../core/services/alert.service';
import { getApiErrorMessage } from '../../core/http/error-message.util';
import { MandatResponse, StatutMandat } from '../admin/models/admin-api.types';
import { ProprietaireEspaceService } from './services/proprietaire-espace.service';

@Component({
  selector: 'app-proprietaire-mandats',
  standalone: true,
  imports: [CommonModule],
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
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Chargement impossible des mandats.'))
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

  async demanderResiliation(id: string, numMandat: string): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Demander la résiliation ?',
      text: `Validation administrateur requise. ${numMandat}`,
      confirmButtonText: 'Envoyer la demande',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;
    this.actionId.set(id);
    this.api
      .demanderResiliationMandat(id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Demande envoyée', 'Votre demande a été transmise.');
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

