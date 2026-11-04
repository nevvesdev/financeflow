import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AutenticacaoService } from './autenticacao.service';
import { ROTAS_PUBLICAS } from './rotas-publicas';

export const tokenInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  const token = inject(AutenticacaoService).tokenValido();
  const deveAutenticar = requisicao.url.startsWith('/api/') && !ROTAS_PUBLICAS.includes(requisicao.url);

  if (token === null || !deveAutenticar) {
    return proximo(requisicao);
  }
  return proximo(requisicao.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
