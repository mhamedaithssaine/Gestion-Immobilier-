import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { AuthService } from '../../../core/services/auth.service';

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
  form = {
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    password: ''
  };

  async submit(): Promise<void> {
    this.errorMessage = '';
    this.successMessage = '';
    this.loading.set(true);
    try {
      await this.authService.register({ ...this.form });
      this.successMessage = "Inscription envoyee. Attends l'activation admin.";
      await this.router.navigateByUrl('/login');
    } catch (error) {
      this.errorMessage = getApiErrorMessage(error, "Inscription echouee.");
    } finally {
      this.loading.set(false);
    }
  }
}
