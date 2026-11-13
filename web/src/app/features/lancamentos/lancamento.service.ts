import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { FiltroLancamentos, Lancamento, Ordenacao, Pagina } from './lancamento.modelos';

@Injectable({ providedIn: 'root' })
export class LancamentoService {
  private readonly http = inject(HttpClient);

  listar(
    filtro: FiltroLancamentos,
    pagina: number,
    tamanho: number,
    ordenacao: Ordenacao,
  ): Observable<Pagina<Lancamento>> {
    let params = new HttpParams()
      .set('page', pagina)
      .set('size', tamanho)
      .set('sort', `${ordenacao.campo},${ordenacao.direcao}`);

    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor !== undefined && valor !== null && valor !== '') {
        params = params.set(chave, String(valor));
      }
    }
    return this.http.get<Pagina<Lancamento>>('/api/lancamentos', { params });
  }

  marcarComoPaga(id: string, dataPagamento?: string): Observable<Lancamento> {
    const corpo = dataPagamento ? { dataPagamento } : null;
    return this.http.post<Lancamento>(`/api/lancamentos/${id}/pagamento`, corpo);
  }

  desfazerPagamento(id: string): Observable<Lancamento> {
    return this.http.delete<Lancamento>(`/api/lancamentos/${id}/pagamento`);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`/api/lancamentos/${id}`);
  }
}