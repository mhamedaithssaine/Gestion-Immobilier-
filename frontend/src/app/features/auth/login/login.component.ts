import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  errorMessage = '';
  identifier = '';
  password = '';

  submit(): void {
    this.errorMessage = '';
    this.loading.set(true);
    this.authService
      .login(this.identifier.trim(), this.password)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          if (this.authService.hasRole('ROLE_ADMIN')) {
            void this.router.navigateByUrl('/admin');
          } else if (this.authService.hasRole('ROLE_AGENT')) {
            void this.router.navigateByUrl('/agence');
          } else if (this.authService.hasRole('ROLE_PROPRIETAIRE')) {
            void this.router.navigateByUrl('/proprietaire');
          } else if (this.authService.hasRole('ROLE_CLIENT')) {
            void this.router.navigateByUrl('/locataire');
          } else {
            void this.router.navigateByUrl('/');
          }
        },
        error: (error: unknown) => {
          this.errorMessage = getApiErrorMessage(error, 'Connexion echouee.');
        }
      });
  }
}
