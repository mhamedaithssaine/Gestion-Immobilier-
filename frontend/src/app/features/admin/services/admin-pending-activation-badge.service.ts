import { Injectable, signal } from '@angular/core';

/** Compteur partagé pour le badge admin (comptes à activer). */
@Injectable({ providedIn: 'root' })
export class AdminPendingActivationBadgeService {
  readonly count = signal(0);

  setCount(n: number): void {
    this.count.set(Number.isFinite(n) && n > 0 ? Math.floor(n) : 0);
  }
}
