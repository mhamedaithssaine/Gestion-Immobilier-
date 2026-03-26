import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-shell.component.html',
  styleUrl: './admin-shell.component.scss'
})
export class AdminShellComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly expandedMenu = signal<'users' | null>(
    this.router.url.startsWith('/admin/utilisateurs') ? 'users' : null
  );

  readonly isAdmin = computed(() => this.auth.hasRole('ROLE_ADMIN'));
  readonly rolesLabel = computed(() => this.auth.roles().join(', ') || '—');

  toggleMenu(menu: 'users'): void {
    this.expandedMenu.set(this.expandedMenu() === menu ? null : menu);
  }

  isUsersRouteActive(): boolean {
    return this.router.url.startsWith('/admin/utilisateurs');
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
