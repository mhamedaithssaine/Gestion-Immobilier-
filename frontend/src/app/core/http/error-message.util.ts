import { HttpErrorResponse } from '@angular/common/http';

export function getApiErrorMessage(err: unknown, fallback = 'Une erreur est survenue.'): string {
  if (err instanceof HttpErrorResponse) {
    if (err.error && typeof err.error === 'object') {
      const message = (err.error as { message?: unknown }).message;
      if (typeof message === 'string' && message.trim()) {
        return message;
      }
    }
    if (typeof err.message === 'string' && err.message.trim()) {
      return err.message;
    }
  }
  return fallback;
}
