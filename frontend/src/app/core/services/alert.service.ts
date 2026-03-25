import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

export type ConfirmOptions = {
  title: string;
  text: string;
  confirmButtonText?: string;
  cancelButtonText?: string;
  icon?: 'warning' | 'question' | 'info' | 'success' | 'error';
};

@Injectable({ providedIn: 'root' })
export class AlertService {
  async confirm(options: ConfirmOptions): Promise<boolean> {
    const result = await Swal.fire({
      title: options.title,
      text: options.text,
      icon: options.icon ?? 'warning',
      showCancelButton: true,
      focusCancel: true,
      confirmButtonText: options.confirmButtonText ?? 'Confirmer',
      cancelButtonText: options.cancelButtonText ?? 'Annuler',
      confirmButtonColor: '#e97300',
      cancelButtonColor: '#829ab1',
      reverseButtons: true
    });
    return result.isConfirmed;
  }

  async success(title: string, text?: string): Promise<void> {
    await Swal.fire({
      title,
      text,
      icon: 'success',
      confirmButtonColor: '#e97300'
    });
  }

  async error(title: string, text?: string): Promise<void> {
    await Swal.fire({
      title,
      text,
      icon: 'error',
      confirmButtonColor: '#e97300'
    });
  }
}

