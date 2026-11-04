import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AutenticacaoService } from './autenticacao.service';

export const autenticadoGuard: CanActivateFn = (_rota, estado) => {
  const autenticacao = inject(AutenticacaoService);
  const roteador = inject(Router);

  return autenticacao.sessaoValida()
    ? true
    : roteador.createUrlTree(['/login'], { queryParams: { retorno: estado.url } });
};

export const visitanteGuard: CanActivateFn = () => {
  const autenticacao = inject(AutenticacaoService);
  const roteador = inject(Router);

  return autenticacao.sessaoValida() ? roteador.createUrlTree(['/painel']) : true;
};
