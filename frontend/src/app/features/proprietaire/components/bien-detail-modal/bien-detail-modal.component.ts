import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { BienResponse, ContratResponse, StatutBien } from '../../../admin/models/admin-api.types';

@Component({
  selector: 'app-bien-detail-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bien-detail-modal.component.html',
  styleUrl: './bien-detail-modal.component.scss'
})
export class BienDetailModalComponent {
  @Input({ required: true }) bien!: BienResponse;
  @Input() contrats: ContratResponse[] = [];
  @Output() close = new EventEmitter<void>();

  readonly imageIndex = signal(0);

  mainImage(): string | null {
    const imgs = this.bien?.images ?? [];
    if (!imgs.length) return null;
    return imgs[this.imageIndex()] ?? imgs[0];
  }

  selectImage(i: number): void {
    this.imageIndex.set(i);
  }

  labelStatut(s: StatutBien): string {
    const m: Record<StatutBien, string> = {
      DISPONIBLE: 'Disponible',
      LOUE: 'Loué',
      VENDU: 'Vendu',
      SOUS_COMPROMIS: 'Sous compromis'
    };
    return m[s] ?? s;
  }
}

