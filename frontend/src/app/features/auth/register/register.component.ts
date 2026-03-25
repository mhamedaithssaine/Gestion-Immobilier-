import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import {
  AuthService,
  PublicRegistrationRole
} from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  errorMessage = '';
  successMessage = '';

  readonly clientRoleOptions: { value: PublicRegistrationRole; label: string }[] = [
    { value: 'ROLE_CLIENT', label: 'Locataire (client)' },
    { value: 'ROLE_PROPRIETAIRE', label: 'Propriétaire' }
  ];

  form = {
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    password: '',
    role: 'ROLE_CLIENT' as PublicRegistrationRole
  };

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.loading.set(true);
    const { role, ...rest } = this.form;
    this.authService
      .register({ ...rest, roles: [role] })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.successMessage = "Inscription envoyee. Attends l'activation admin.";
          void this.router.navigateByUrl('/login');
        },
        error: (error: unknown) => {
          this.errorMessage = getApiErrorMessage(error, "Inscription echouee.");
        }
      });
  }
}
