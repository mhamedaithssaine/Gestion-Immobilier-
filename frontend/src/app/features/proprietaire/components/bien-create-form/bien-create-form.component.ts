import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BienResponse, StatutBien } from '../../../admin/models/admin-api.types';
import { ProprietaireBienCreatePayload } from '../../services/proprietaire-espace.service';

export type BienFormSubmitEvent =
  | { mode: 'create'; payload: ProprietaireBienCreatePayload; images: File[] }
  | { mode: 'edit'; bienId: string; payload: ProprietaireBienCreatePayload; images: File[] };

@Component({
  selector: 'app-bien-create-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bien-create-form.component.html',
  styleUrl: './bien-create-form.component.scss'
})
export class BienCreateFormComponent implements OnChanges {
  @Input() mode: 'create' | 'edit' = 'create';
  /** Id du bien en mode édition (requis pour l’événement submit) */
  @Input() editBienId: string | null = null;
  /** Données complètes renvoyées par GET /biens/{id} pour préremplir */
  @Input() prefill: BienResponse | null = null;
  @Input() submitting = false;

  @Output() close = new EventEmitter<void>();
  @Output() formSubmit = new EventEmitter<BienFormSubmitEvent>();

  type: 'APPARTEMENT' | 'MAISON' = 'APPARTEMENT';
  titre = '';
  surface: number | null = null;
  prixBase: number | null = null;
  statut: StatutBien = 'DISPONIBLE';

  rue = '';
  ville = '';
  codePostal = '';
  pays = 'France';

  etage: number | null = 0;
  ascenseur = false;
  surfaceTerrain: number | null = null;
  garage = false;

  /** Nouveaux fichiers (création ou ajout en édition) */
  images: File[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['prefill'] && this.prefill && this.mode === 'edit') {
      this.applyPrefill(this.prefill);
    }
  }

  /**
   * Déduit le type à partir du polymorphisme backend (préfère `type` si présent).
   */
  private inferType(b: BienResponse): 'APPARTEMENT' | 'MAISON' {
    if (b.type === 'APPARTEMENT' || b.type === 'MAISON') {
      return b.type;
    }
    const looksAppart = b.etage !== undefined && b.etage !== null;
    const looksMaison = b.surfaceTerrain !== undefined && b.surfaceTerrain !== null;
    if (looksMaison && !looksAppart) {
      return 'MAISON';
    }
    if (looksAppart && !looksMaison) {
      return 'APPARTEMENT';
    }
    if (b.ascenseur !== undefined) {
      return 'APPARTEMENT';
    }
    if (b.garage !== undefined) {
      return 'MAISON';
    }
    return 'APPARTEMENT';
  }

  private applyPrefill(b: BienResponse): void {
    this.type = this.inferType(b);
    this.titre = b.titre;
    this.surface = b.surface;
    this.prixBase = typeof b.prixBase === 'number' ? b.prixBase : Number(b.prixBase);
    this.statut = b.statut;
    this.rue = b.adresse?.rue ?? '';
    this.ville = b.adresse?.ville ?? '';
    this.codePostal = b.adresse?.codePostal ?? '';
    this.pays = b.adresse?.pays ?? 'France';
    if (this.type === 'APPARTEMENT') {
      this.etage = b.etage ?? 0;
      this.ascenseur = b.ascenseur ?? false;
      this.surfaceTerrain = null;
      this.garage = false;
    } else {
      this.surfaceTerrain = b.surfaceTerrain ?? 0;
      this.garage = b.garage ?? false;
      this.etage = 0;
      this.ascenseur = false;
    }
    this.images = [];
  }

  /** En création uniquement : évite de garder des champs de l’autre type dans le state */
  onTypeChange(): void {
    if (this.mode === 'edit') {
      return;
    }
    if (this.type === 'APPARTEMENT') {
      this.surfaceTerrain = null;
      this.garage = false;
      this.etage = this.etage ?? 0;
      this.ascenseur = this.ascenseur ?? false;
    } else {
      this.etage = null;
      this.ascenseur = false;
      this.surfaceTerrain = this.surfaceTerrain ?? 0;
      this.garage = this.garage ?? false;
    }
  }

  onImagesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.images = input.files ? Array.from(input.files) : [];
  }

  submit(): void {
    if (!this.titre || !this.surface || !this.prixBase) return;
    if (!this.rue || !this.ville || !this.codePostal || !this.pays) return;

    const payload: ProprietaireBienCreatePayload = {
      type: this.type,
      titre: this.titre,
      surface: this.surface,
      prixBase: this.prixBase,
      statut: this.statut,
      adresse: {
        rue: this.rue,
        ville: this.ville,
        codePostal: this.codePostal,
        pays: this.pays
      },
      ...(this.type === 'APPARTEMENT'
        ? { etage: this.etage ?? 0, ascenseur: this.ascenseur }
        : { surfaceTerrain: this.surfaceTerrain ?? 0, garage: this.garage })
    };

    if (this.mode === 'edit') {
      const id = this.editBienId ?? this.prefill?.id;
      if (!id) return;
      this.formSubmit.emit({ mode: 'edit', bienId: id, payload, images: this.images });
      return;
    }

    this.formSubmit.emit({ mode: 'create', payload, images: this.images });
  }
}
