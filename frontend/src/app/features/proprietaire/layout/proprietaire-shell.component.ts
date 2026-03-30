import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-proprietaire-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './proprietaire-shell.component.html',
  styleUrl: './proprietaire-shell.component.scss'
})
export class ProprietaireShellComponent {
  private readonly auth = inject(AuthService);

  logout(): void {
    this.auth.logout();
  }
}

