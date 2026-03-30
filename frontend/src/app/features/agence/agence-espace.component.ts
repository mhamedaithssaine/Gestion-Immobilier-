import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { AlertService } from '../../core/services/alert.service';
import { getApiErrorMessage } from '../../core/http/error-message.util';
import {
  AgenceDemandeEnAttenteResponse,
  AgenceEspaceAgentResponse,
  AgenceResponse,
  StatutAgence
} from '../admin/models/admin-api.types';
import { AgentAgenceService } from '../admin/services/agent-agence.service';

@Component({
  selector: 'app-agence-espace',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agence-espace.component.html',
  styleUrl: './agence-espace.component.scss'
})
export class AgenceEspaceComponent implements OnInit {
  private readonly api = inject(AgentAgenceService);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly deleting = signal(false);
  readonly error = signal<string | null>(null);
  readonly espace = signal<AgenceEspaceAgentResponse | null>(null);

  form = { nom: '', email: '', telephone: '', adresse: '', ville: '' };

  ngOnInit(): void {
    this.load();
  }

  readonly agence = (): AgenceResponse | null => this.espace()?.agence ?? null;
  readonly demande = (): AgenceDemandeEnAttenteResponse | null =>
    this.espace()?.demandeEnAttente ?? null;

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .getMonAgence()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => {
          this.espace.set(r);
          const a = r.agence;
          this.form = {
            nom: a.nom ?? '',
            email: a.email ?? '',
            telephone: a.telephone ?? '',
            adresse: a.adresse ?? '',
            ville: a.ville ?? ''
          };
        },
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Impossible de charger votre agence.'))
      });
  }

  labelStatut(s: StatutAgence): string {
    const m: Record<StatutAgence, string> = {
      PENDING: 'En attente de validation',
      ACTIVE: 'Active',
      REJECTED: 'Rejetée',
      SUSPENDED: 'Suspendue'
    };
    return m[s] ?? s;
  }

  labelTypeDemande(t: string): string {
    return t === 'SUPPRESSION' ? 'Demande de clôture' : 'Mise à jour de la fiche';
  }

  async soumettreMiseAJour(): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Soumettre la demande de modification ?',
      text: 'Un administrateur devra approuver les changements avant qu’ils soient visibles.',
      confirmButtonText: 'Soumettre',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;

    this.saving.set(true);
    this.error.set(null);
    this.api
      .soumettreDemandeMiseAJour({ ...this.form })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: async (r) => {
          this.espace.set(r);
          await this.alert.success('Demande envoyée', 'En attente de validation par un administrateur.');
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Envoi impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }

  async soumettreSuppression(): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Demander la clôture de l’agence ?',
      text:
        'Si un administrateur approuve, l’agence sera suspendue et les comptes agents désactivés. Cette action est réservée à la fermeture définitive.',
      confirmButtonText: 'Soumettre la demande',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;

    this.deleting.set(true);
    this.error.set(null);
    this.api
      .soumettreDemandeSuppression()
      .pipe(finalize(() => this.deleting.set(false)))
      .subscribe({
        next: async (r) => {
          this.espace.set(r);
          await this.alert.success('Demande enregistrée', 'En attente de validation par un administrateur.');
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Envoi impossible.');
          this.error.set(msg);
          await this.alert.error('Erreur', msg);
        }
      });
  }
}
