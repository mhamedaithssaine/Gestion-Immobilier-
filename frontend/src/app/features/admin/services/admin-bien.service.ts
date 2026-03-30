import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { ApiRetour } from '../../../core/http/api-types';
import { BienResponse } from '../models/admin-api.types';

@Injectable({ providedIn: 'root' })
export class AdminBienService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/proprietaire/biens`;

  list(): Observable<BienResponse[]> {
    return this.http.get<ApiRetour<BienResponse[]>>(this.base).pipe(map((r) => r.data));
  }

  delete(id: string): Observable<void> {
    return this.http
      .delete<ApiRetour<null>>(`${this.base}/${id}`)
      .pipe(map(() => undefined));
  }
}

