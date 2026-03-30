import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SpaceModalService } from '../../services/space-modal.service';

@Component({
  selector: 'app-skna-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './skna-header.component.html',
  styleUrl: './skna-header.component.scss'
})
export class SknaHeaderComponent {
  private readonly spaceModal = inject(SpaceModalService);

  readonly navLinks = ['Louer', 'Gestion locative', 'Location vacances', 'Actualités'] as const;

  openMonEspace(): void {
    this.spaceModal.open();
  }
}
