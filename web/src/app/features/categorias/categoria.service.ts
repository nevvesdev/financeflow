import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { TipoLancamento } from '../../core/api/dominio';
import { Categoria } from './categoria.modelos';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly http = inject(HttpClient);

  listar(tipo?: TipoLancamento): Observable<Categoria[]> {
    const params = tipo ? new HttpParams().set('tipo', tipo) : new HttpParams();
    return this.http.get<Categoria[]>('/api/categorias', { params });
  }
}