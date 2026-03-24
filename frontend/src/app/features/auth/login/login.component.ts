import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
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

  async submit(): Promise<void> {
    this.errorMessage = '';
    this.loading.set(true);
    try {
      await this.authService.login(this.identifier.trim(), this.password);
      await this.router.navigateByUrl('/dashboard');
    } catch (error) {
      this.errorMessage = getApiErrorMessage(error, 'Connexion echouee.');
    } finally {
      this.loading.set(false);
    }
  }
}
