import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AutenticacaoService } from './autenticacao.service';
import { ROTAS_PUBLICAS } from './rotas-publicas';

export const erroSessaoInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const autenticacao = inject(AutenticacaoService);

  return proximo(requisicao).pipe(
    catchError((erro: unknown) => {
      const sessaoRejeitada =
        erro instanceof HttpErrorResponse &&
        erro.status === 401 &&
        !ROTAS_PUBLICAS.includes(requisicao.url);
      if (sessaoRejeitada) {
        autenticacao.encerrarPorExpiracao();
      }
      return throwError(() => erro);
    }),
  );
};
