import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { getApiErrorMessage } from '../../../core/http/error-message.util';
import { AgencePublicService } from '../../../core/services/agence-public.service';

@Component({
  selector: 'app-register-agence',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register-agence.component.html',
  styleUrl: './register-agence.component.scss'
})
export class RegisterAgenceComponent {
  private readonly agencePublic = inject(AgencePublicService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly submitted = signal(false);
  errorMessage = '';
  successMessage = '';

  private static readonly PERSONAL_EMAIL_DOMAINS = new Set([
    'gmail.com',
    'googlemail.com',
    'yahoo.com',
    'yahoo.fr',
    'hotmail.com',
    'hotmail.fr',
    'outlook.com',
    'live.com',
    'live.fr',
    'msn.com',
    'free.fr',
    'orange.fr',
    'sfr.fr',
    'laposte.net',
    'wanadoo.fr',
    'voila.fr',
    'aol.com',
    'icloud.com',
    'me.com',
    'mac.com',
    'protonmail.com',
    'proton.me',
    'zoho.com',
    'yandex.com',
    'mail.com',
    'gmx.com',
    'gmx.fr'
  ]);

  form = {
    nom: '',
    email: '',
    telephone: '',
    adresse: '',
    ville: '',
    agentUsername: '',
    agentEmail: '',
    agentFirstName: '',
    agentLastName: '',
    agentPassword: ''
  };

  isBusinessEmail(value: string): boolean {
    const email = (value ?? '').trim().toLowerCase();
    const at = email.indexOf('@');
    if (at <= 0 || at >= email.length - 1) return false;
    const domain = email.substring(at + 1);
    return !RegisterAgenceComponent.PERSONAL_EMAIL_DOMAINS.has(domain);
  }

  shouldShow(
    control: { touched: boolean | null; invalid: boolean | null } | null | undefined
  ): boolean {
    return !!control && !!control.invalid && (!!control.touched || this.submitted());
  }

  submit(formRef: NgForm): void {
    this.submitted.set(true);
    this.errorMessage = '';
    this.successMessage = '';
    if (formRef.invalid || !this.isBusinessEmail(this.form.email)) {
      this.errorMessage = 'Les données fournies sont invalides. Vérifiez les champs indiqués.';
      return;
    }
    this.loading.set(true);
    this.agencePublic
      .inscrire({ ...this.form })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.successMessage = "Inscription agence envoyee. Attends la validation de l'administrateur.";
          this.submitted.set(false);
          void this.router.navigateByUrl('/login');
        },
        error: (error: unknown) => {
          this.errorMessage = getApiErrorMessage(error, "Inscription agence echouee.");
        }
      });
  }
}

