import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SpaceModalService {
  /** Modale « Mon Espace » (choix client / candidat). */
  readonly isOpen = signal(false);

  open(): void {
    this.isOpen.set(true);
  }

  close(): void {
    this.isOpen.set(false);
  }
}
