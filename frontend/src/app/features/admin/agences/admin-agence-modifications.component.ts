import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { AlertService } from '../../../core/services/alert.service';
import { AgenceModificationDemandeAdminResponse } from '../models/admin-api.types';
import { AdminAgenceModificationService } from '../services/admin-agence-modification.service';

@Component({
  selector: 'app-admin-agence-modifications',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-agence-modifications.component.html',
  styleUrl: './admin-agence-modifications.component.scss'
})
export class AdminAgenceModificationsComponent implements OnInit {
  private readonly api = inject(AdminAgenceModificationService);
  private readonly alert = inject(AlertService);

  readonly loading = signal(false);
  readonly actionId = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly rows = signal<AgenceModificationDemandeAdminResponse[]>([]);
  readonly rejectComment = signal<Record<string, string>>({});

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .listerEnAttente()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => this.rows.set(r),
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Impossible de charger les demandes.'))
      });
  }

  labelType(t: string): string {
    return t === 'SUPPRESSION' ? 'Clôture / suspension' : 'Mise à jour fiche';
  }

  getComment(id: string): string {
    return this.rejectComment()[id] ?? '';
  }

  setComment(id: string, v: string): void {
    this.rejectComment.update((m) => ({ ...m, [id]: v }));
  }

  async approuver(row: AgenceModificationDemandeAdminResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Approuver cette demande ?',
      text:
        row.type === 'SUPPRESSION'
          ? "L'agence sera suspendue et les comptes agents désactivés."
          : 'Les informations publiques seront mises à jour.',
      confirmButtonText: 'Approuver',
      cancelButtonText: 'Annuler',
      icon: 'question'
    });
    if (!ok) return;
    this.actionId.set(row.id);
    this.api
      .approuver(row.id)
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Demande approuvée', '');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Action impossible.');
          await this.alert.error('Erreur', msg);
        }
      });
  }

  async rejeter(row: AgenceModificationDemandeAdminResponse): Promise<void> {
    const ok = await this.alert.confirm({
      title: 'Rejeter cette demande ?',
      text: this.getComment(row.id) || 'Aucun commentaire',
      confirmButtonText: 'Rejeter',
      cancelButtonText: 'Annuler',
      icon: 'warning'
    });
    if (!ok) return;
    this.actionId.set(row.id);
    this.api
      .rejeter(row.id, this.getComment(row.id))
      .pipe(finalize(() => this.actionId.set(null)))
      .subscribe({
        next: async () => {
          await this.alert.success('Demande rejetée', '');
          this.reload();
        },
        error: async (err: unknown) => {
          const msg = getApiErrorMessage(err, 'Action impossible.');
          await this.alert.error('Erreur', msg);
        }
      });
  }
}
