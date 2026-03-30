import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { getApiErrorMessage, getApiFieldErrors } from '../../../../core/http/error-message.util';
import { AlertService } from '../../../../core/services/alert.service';
import { BienResponse } from '../../../admin/models/admin-api.types';
import { LocataireEspaceService, SpringPage } from '../../services/locataire-espace.service';

export type BienTypeFilter = 'APPARTEMENT' | 'MAISON';

@Component({
  selector: 'app-locataire-biens-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './locataire-biens-page.component.html',
  styleUrl: './locataire-biens-page.component.scss'
})
export class LocataireBiensPageComponent implements OnInit {
  private readonly api = inject(LocataireEspaceService);
  private readonly alert = inject(AlertService);

  readonly typeOptions: ReadonlyArray<{ value: BienTypeFilter; label: string }> = [
    { value: 'APPARTEMENT', label: 'Appartement' },
    { value: 'MAISON', label: 'Maison' }
  ];

  readonly loading = signal(false);
  readonly requesting = signal(false);
  readonly error = signal<string | null>(null);
  readonly imageIndexByBien = signal<Record<string, number>>({});

  readonly page = signal<SpringPage<BienResponse>>({
    content: [],
    totalElements: 0,
    totalPages: 0,
    size: 8,
    number: 0,
    first: true,
    last: true
  });

  query = '';
  ville = '';
  minPrix: number | null = null;
  maxPrix: number | null = null;
  minSurface: number | null = null;
  maxSurface: number | null = null;

  /** Types sélectionnés pour le filtre (vide = tous). */
  readonly selectedTypes = signal<BienTypeFilter[]>([]);
  readonly typeMenuOpen = signal(false);

  /** Modal détail */
  selectedBien = signal<BienResponse | null>(null);
  detailImageIndex = signal(0);

  /** Modal demande */
  demandeBien = signal<BienResponse | null>(null);
  demandeDateDebut = '';
  demandeDateFin = '';
  readonly demandeFieldErrors = signal<Record<string, string>>({});
  readonly demandeGlobalError = signal<string | null>(null);

  ngOnInit(): void {
    this.load(0);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(ev: MouseEvent): void {
    if (!this.typeMenuOpen()) return;
    const t = ev.target as HTMLElement;
    if (!t.closest('.type-multiselect')) {
      this.typeMenuOpen.set(false);
    }
  }

  typesSummary(): string {
    const n = this.selectedTypes().length;
    if (n === 0) return 'Tous les types';
    if (n === 1) return '1 type de bien';
    return `${n} types de biens`;
  }

  isTypeSelected(value: BienTypeFilter): boolean {
    return this.selectedTypes().includes(value);
  }

  toggleTypeMenu(ev: Event): void {
    ev.stopPropagation();
    this.typeMenuOpen.update((o) => !o);
  }

  toggleType(value: BienTypeFilter, ev?: Event): void {
    ev?.stopPropagation();
    const cur = this.selectedTypes();
    const next = cur.includes(value) ? cur.filter((t) => t !== value) : [...cur, value];
    this.selectedTypes.set(next);
  }

  load(page: number): void {
    this.loading.set(true);
    this.error.set(null);
    const types = this.selectedTypes();
    this.api
      .listBiens({
        q: this.query,
        ville: this.ville,
        minPrix: this.minPrix,
        maxPrix: this.maxPrix,
        minSurface: this.minSurface,
        maxSurface: this.maxSurface,
        types: types.length ? types : undefined,
        page,
        size: this.page().size || 8
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (data) => {
          this.page.set(data);
          const nextIndexes: Record<string, number> = {};
          (data.content ?? []).forEach((b) => (nextIndexes[b.id] = 0));
          this.imageIndexByBien.set(nextIndexes);
        },
        error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Chargement des biens impossible.'))
      });
  }

  search(): void {
    this.typeMenuOpen.set(false);
    this.load(0);
  }

  resetFilters(): void {
    this.query = '';
    this.ville = '';
    this.minPrix = null;
    this.maxPrix = null;
    this.minSurface = null;
    this.maxSurface = null;
    this.selectedTypes.set([]);
    this.typeMenuOpen.set(false);
    this.load(0);
  }

  prevPage(): void {
    if (!this.page().first) this.load(this.page().number - 1);
  }

  nextPage(): void {
    if (!this.page().last) this.load(this.page().number + 1);
  }

  currentImage(bien: BienResponse): string | null {
    if (!bien.images.length) return null;
    const index = this.imageIndexByBien()[bien.id] ?? 0;
    return bien.images[index] ?? bien.images[0];
  }

  prevImage(bien: BienResponse): void {
    if (bien.images.length <= 1) return;
    const curr = this.imageIndexByBien()[bien.id] ?? 0;
    const next = curr === 0 ? bien.images.length - 1 : curr - 1;
    this.imageIndexByBien.set({ ...this.imageIndexByBien(), [bien.id]: next });
  }

  nextImage(bien: BienResponse): void {
    if (bien.images.length <= 1) return;
    const curr = this.imageIndexByBien()[bien.id] ?? 0;
    const next = (curr + 1) % bien.images.length;
    this.imageIndexByBien.set({ ...this.imageIndexByBien(), [bien.id]: next });
  }

  openDetail(id: string): void {
    this.api.getBien(id).subscribe({
      next: (bien) => {
        this.selectedBien.set(bien);
        this.detailImageIndex.set(0);
      },
      error: (err: unknown) => this.error.set(getApiErrorMessage(err, 'Impossible de charger le détail.'))
    });
  }

  closeDetail(): void {
    this.selectedBien.set(null);
    this.detailImageIndex.set(0);
  }

  selectDetailImage(index: number): void {
    this.detailImageIndex.set(index);
  }

  detailImageUrl(): string | null {
    const bien = this.selectedBien();
    if (!bien || !bien.images.length) return null;
    return bien.images[this.detailImageIndex()] ?? bien.images[0];
  }

  openDemandeModal(bien: BienResponse): void {
    this.demandeBien.set(bien);
    this.demandeDateDebut = '';
    this.demandeDateFin = '';
    this.demandeFieldErrors.set({});
    this.demandeGlobalError.set(null);
  }

  openDemandeFromDetail(): void {
    const b = this.selectedBien();
    if (b) this.openDemandeModal(b);
  }

  closeDemandeModal(): void {
    this.demandeBien.set(null);
    this.demandeDateDebut = '';
    this.demandeDateFin = '';
    this.demandeFieldErrors.set({});
    this.demandeGlobalError.set(null);
  }

  private validateDemandeClient(): boolean {
    const errs: Record<string, string> = {};
    if (!this.demandeDateDebut.trim()) {
      errs['dateDebut'] = 'La date de début est obligatoire.';
    }
    if (!this.demandeDateFin.trim()) {
      errs['dateFin'] = 'La date de fin est obligatoire.';
    }
    this.demandeFieldErrors.set(errs);
    this.demandeGlobalError.set(null);
    return Object.keys(errs).length === 0;
  }

  submitDemande(): void {
    const bien = this.demandeBien();
    if (!bien) return;
    if (!this.validateDemandeClient()) return;

    this.requesting.set(true);
    this.demandeFieldErrors.set({});
    this.demandeGlobalError.set(null);

    this.api
      .demanderLocation(bien.id, this.demandeDateDebut, this.demandeDateFin)
      .pipe(finalize(() => this.requesting.set(false)))
      .subscribe({
        next: async (contrat) => {
          const suite =
            contrat.statut === 'EN_ATTENTE_VALIDATION_AGENT'
              ? ' En attente de validation par l’agent mandaté.'
              : ' En attente du propriétaire.';
          await this.alert.success('Demande envoyée', `N° ${contrat.numContrat}.${suite}`);
          this.closeDemandeModal();
          this.closeDetail();
          this.load(this.page().number);
        },
        error: (err: unknown) => {
          const fields = getApiFieldErrors(err);
          if (fields) {
            this.demandeFieldErrors.set(fields);
            this.demandeGlobalError.set(getApiErrorMessage(err, 'Vérifiez les champs ci-dessous.'));
          } else {
            this.demandeGlobalError.set(getApiErrorMessage(err, 'Envoi de la demande impossible.'));
            this.demandeFieldErrors.set({});
          }
        }
      });
  }
}
