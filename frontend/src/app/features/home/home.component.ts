import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { SpaceModalService } from '../../core/services/space-modal.service';
import { BienResponse } from '../admin/models/admin-api.types';
import { PublicBienService } from './public-bien.service';

export interface HomeFaqItem {
  question: string;
  answer: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  private readonly spaceModal = inject(SpaceModalService);
  private readonly publicBiens = inject(PublicBienService);

  readonly agencyCount = 300;
  readonly agencyCountry = 'Maroc';
  readonly agencyFinderLead =
    "Où que vous soyez au Maroc, une agence Skna est à proximité pour vous accompagner dans l'achat, la vente, la location ou la gestion de votre bien immobilier.";

  readonly faqItems: HomeFaqItem[] = [
    {
      question: "Comment fonctionne l'espace client Skna ?",
      answer:
        'Votre espace client vous donne accès à vos dossiers, documents, paiements et au suivi de votre bien en temps réel. Cliquez sur "Mon Espace" pour vous connecter.'
    },
    {
      question: 'Skna gère-t-elle des biens en location et en vente ?',
      answer:
        "Oui, Skna accompagne aussi bien les projets d'achat, de vente que de location et de gestion locative, avec des outils adaptés à chaque situation."
    },
    {
      question: 'Comment consulter les biens disponibles ?',
      answer:
        "Les biens disponibles sont affichés directement sur la page d'accueil. Utilisez la barre de recherche pour filtrer par mot-clé ou par ville. Pour accéder à l'ensemble du catalogue, connectez-vous à votre espace client."
    },
    {
      question: 'Comment créer mon compte Skna ?',
      answer:
        'Cliquez sur "Créer un compte" depuis la page d\'accueil. Une fois inscrit, vous accédez à votre tableau de bord personnel pour gérer vos dossiers et suivre vos démarches.'
    },
    {
      question: 'Puis-je confier la gestion de mon bien à Skna ?',
      answer:
        'Oui. Depuis votre espace client, vous pouvez soumettre votre bien et notre équipe vous contacte pour établir un mandat de gestion adapté à votre situation.'
    },
    {
      question: "Comment naviguer entre plusieurs photos d'un bien ?",
      answer:
        'Sur chaque fiche bien, des boutons de navigation (‹ ›) permettent de faire défiler les photos. Un compteur indique votre position (ex : 2 / 5).'
    }
  ];

  readonly loading = signal(false);
  readonly biens = signal<BienResponse[]>([]);
  readonly totalPages = signal(0);
  readonly page = signal(0);
  readonly imageIndexByBien = signal<Record<string, number>>({});
  readonly activeFaqIndex = signal(0);

  query = '';
  ville = '';
  agencyLocalisation = '';
  agencyActivites = '';

  constructor() {
    this.load(0);
  }

  openMonEspace(): void {
    this.spaceModal.open();
  }

  load(page: number): void {
    this.loading.set(true);
    this.publicBiens
      .list({ q: this.query, ville: this.ville, page, size: 6 })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (res) => {
          this.biens.set(res.content ?? []);
          this.totalPages.set(res.totalPages ?? 0);
          this.page.set(res.number ?? page);
          const nextIndexes: Record<string, number> = {};
          (res.content ?? []).forEach((b) => (nextIndexes[b.id] = 0));
          this.imageIndexByBien.set(nextIndexes);
        }
      });
  }

  searchBiens(): void {
    this.load(0);
  }

  prev(): void {
    if (this.page() > 0) this.load(this.page() - 1);
  }

  next(): void {
    if (this.page() + 1 < this.totalPages()) this.load(this.page() + 1);
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

  toggleFaq(index: number): void {
    this.activeFaqIndex.update((current) => (current === index ? -1 : index));
  }

  isFaqOpen(index: number): boolean {
    return this.activeFaqIndex() === index;
  }

  searchAgences(): void {
    const loc = this.agencyLocalisation.trim();
    if (loc) {
      this.ville = loc;
    }
    const act = this.agencyActivites.trim();
    if (act) {
      this.query = act;
    }
    this.searchBiens();
    document.getElementById('catalogue')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  detectAgenceLocation(): void {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      return;
    }
    navigator.geolocation.getCurrentPosition(
      () => {
        this.agencyLocalisation = 'Autour de moi';
      },
      () => {
        /* refus ou indisponible : silencieux */
      }
    );
  }
}
