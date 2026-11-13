import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Painel } from './painel.modelos';

@Injectable({ providedIn: 'root' })
export class PainelService {
  private readonly http = inject(HttpClient);

  buscar(mes?: string): Observable<Painel> {
    const params = mes ? new HttpParams().set('mes', mes) : new HttpParams();
    return this.http.get<Painel>('/api/painel', { params });
  }
}
