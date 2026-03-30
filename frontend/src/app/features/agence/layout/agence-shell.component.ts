import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-agence-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './agence-shell.component.html',
  styleUrl: './agence-shell.component.scss'
})
export class AgenceShellComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly rolesLabel = computed(() => this.auth.roles().join(', ') || '—');

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
