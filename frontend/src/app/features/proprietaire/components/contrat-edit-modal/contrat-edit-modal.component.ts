import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ContratResponse, StatutBail } from '../../../admin/models/admin-api.types';
import { ProprietaireContratUpdatePayload } from '../../services/proprietaire-espace.service';

export type ContratEditSubmitEvent = {
  contratId: string;
  payload: ProprietaireContratUpdatePayload;
};

export type ContratStatusSubmitEvent = {
  contratId: string;
  statut: StatutBail;
};

@Component({
  selector: 'app-contrat-edit-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contrat-edit-modal.component.html',
  styleUrl: './contrat-edit-modal.component.scss'
})
export class ContratEditModalComponent implements OnChanges {
  @Input({ required: true }) contrat!: ContratResponse;
  @Input() submitting = false;

  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<ContratEditSubmitEvent>();
  @Output() saveStatus = new EventEmitter<ContratStatusSubmitEvent>();

  dateDebut = '';
  dateFin = '';
  loyerHC: number | null = null;
  charges: number | null = null;
  dateSignature = '';
  documentUrl = '';
  statut: StatutBail = 'EN_ATTENTE';

  readonly statutOptions: { value: StatutBail; label: string }[] = [
    { value: 'EN_ATTENTE', label: 'En attente' },
    { value: 'ACTIF', label: 'Actif' },
    { value: 'RESILIE', label: 'Résilié' },
    { value: 'TERMINE', label: 'Terminé' }
  ];

  /** Le propriétaire ne change pas le statut des dossiers réservés à l’agent ou déjà clos. */
  get statutChangeDisabled(): boolean {
    const s = this.contrat?.statut;
    return (
      s === 'EN_ATTENTE_VALIDATION_AGENT' ||
      s === 'REFUSE' ||
      s === 'RESILIE' ||
      s === 'TERMINE'
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['contrat'] && this.contrat) {
      this.dateDebut = this.contrat.dateDebut ?? '';
      this.dateFin = this.contrat.dateFin ?? '';
      this.loyerHC = this.contrat.loyerHC ?? 0;
      this.charges = this.contrat.charges ?? 0;
      this.dateSignature = this.contrat.dateSignature ?? '';
      this.documentUrl = this.contrat.documentUrl ?? '';
      this.statut = this.contrat.statut;
    }
  }

  submit(): void {
    this.save.emit({
      contratId: this.contrat.id,
      payload: {
        dateDebut: this.dateDebut || null,
        dateFin: this.dateFin || null,
        loyerHC: this.loyerHC ?? null,
        charges: this.charges ?? null,
        dateSignature: this.dateSignature || null,
        documentUrl: this.documentUrl.trim() || null
      }
    });
  }

  submitStatus(): void {
    this.saveStatus.emit({
      contratId: this.contrat.id,
      statut: this.statut
    });
  }
}

