import { Component } from '@angular/core';

@Component({
  selector: 'app-skna-footer',
  standalone: true,
  imports: [],
  templateUrl: './skna-footer.component.html',
  styleUrl: './skna-footer.component.scss'
})
export class SknaFooterComponent {
  readonly year = new Date().getFullYear();

  readonly colSkna = [
    'Nous découvrir',
    'Carrières',
    'Nos offres d’emploi',
    'Espace presse',
    'Avis clients'
  ] as const;

  readonly colSite = [
    'Plan du site',
    'Mentions légales',
    'Politique de protection des données',
    'Conformité',
    'Gestion des cookies'
  ] as const;

  readonly colInfos = [
    'Nous contacter',
    'Trouver une agence',
    'Estimation bien immobilier',
    'Estimation loyer',
    'Actualités'
  ] as const;
}
