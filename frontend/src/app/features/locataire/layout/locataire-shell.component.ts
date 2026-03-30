import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-locataire-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './locataire-shell.component.html',
  styleUrl: './locataire-shell.component.scss'
})
export class LocataireShellComponent {
  private readonly auth = inject(AuthService);

  logout(): void {
    this.auth.logout();
  }
}

