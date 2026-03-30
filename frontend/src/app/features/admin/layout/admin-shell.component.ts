import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { AdminDashboardService } from '../services/admin-dashboard.service';
import { AdminPendingActivationBadgeService } from '../services/admin-pending-activation-badge.service';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-shell.component.html',
  styleUrl: './admin-shell.component.scss'
})
export class AdminShellComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly dashboard = inject(AdminDashboardService);
  readonly pendingBadge = inject(AdminPendingActivationBadgeService);
  readonly expandedMenu = signal<'users' | 'agences' | null>(
    this.router.url.startsWith('/admin/utilisateurs')
      ? 'users'
      : this.router.url.startsWith('/admin/agences')
        ? 'agences'
        : null
  );

  readonly isAdmin = computed(() => this.auth.hasRole('ROLE_ADMIN'));
  readonly rolesLabel = computed(() => this.auth.roles().join(', ') || '—');

  ngOnInit(): void {
    this.refreshPendingActivationCount();
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => this.refreshPendingActivationCount());
  }

  private refreshPendingActivationCount(): void {
    if (!this.auth.hasRole('ROLE_ADMIN') || !this.router.url.startsWith('/admin')) {
      return;
    }
    this.dashboard.nombreComptesEnAttenteActivation().subscribe({
      next: (r) => this.pendingBadge.setCount(r.nombreComptesEnAttenteActivation),
      error: () => this.pendingBadge.setCount(0)
    });
  }

  toggleMenu(menu: 'users' | 'agences'): void {
    this.expandedMenu.set(this.expandedMenu() === menu ? null : menu);
  }

  isUsersRouteActive(): boolean {
    return this.router.url.startsWith('/admin/utilisateurs');
  }

  isAgencesRouteActive(): boolean {
    return this.router.url.startsWith('/admin/agences');
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
