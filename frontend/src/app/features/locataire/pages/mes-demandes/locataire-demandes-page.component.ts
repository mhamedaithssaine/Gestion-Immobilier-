import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { getApiErrorMessage } from '../../../../core/http/error-message.util';
import { ContratResponse } from '../../../admin/models/admin-api.types';
import { LocataireEspaceService } from '../../services/locataire-espace.service';

@Component({
  selector: 'app-locataire-demandes-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './locataire-demandes-page.component.html',
  styleUrl: './locataire-demandes-page.component.scss'
})
export class LocataireDemandesPageComponent implements OnInit {
  private readonly api = inject(LocataireEspaceService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly rows = signal<ContratResponse[]>([]);

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .listMesDemandes()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (rows) => this.rows.set(rows),
        error: (err: unknown) =>
          this.error.set(getApiErrorMessage(err, 'Chargement de vos demandes impossible.'))
      });
  }

  label(statut: ContratResponse['statut']): string {
    return (
      {
        EN_ATTENTE: 'En attente (propriétaire)',
        EN_ATTENTE_VALIDATION_AGENT: 'En attente (agent mandaté)',
        ACTIF: 'Actif',
        REFUSE: 'Refusé',
        RESILIE: 'Résilié',
        TERMINE: 'Terminé'
      }[statut] ?? statut
    );
  }

  badgeClass(statut: ContratResponse['statut']): string {
    if (statut === 'EN_ATTENTE' || statut === 'EN_ATTENTE_VALIDATION_AGENT') return 'waiting';
    if (statut === 'REFUSE') return 'refused';
    if (statut === 'ACTIF') return 'active';
    return '';
  }
}

