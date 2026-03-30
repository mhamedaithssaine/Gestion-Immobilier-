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

/** Erreurs par champ renvoyées par Spring (`MethodArgumentNotValidException` → `errors`). */
export function getApiFieldErrors(err: unknown): Record<string, string> | null {
  if (!(err instanceof HttpErrorResponse) || !err.error || typeof err.error !== 'object') {
    return null;
  }
  const raw = (err.error as { errors?: unknown }).errors;
  if (!raw || typeof raw !== 'object' || Array.isArray(raw)) {
    return null;
  }
  const out: Record<string, string> = {};
  for (const [k, v] of Object.entries(raw as Record<string, unknown>)) {
    if (typeof v === 'string' && v.trim()) {
      out[k] = v;
    }
  }
  return Object.keys(out).length ? out : null;
}
