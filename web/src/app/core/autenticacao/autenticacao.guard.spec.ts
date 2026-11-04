import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { describe, expect, it } from 'vitest';

import { guardarSessao, sessaoExpirada, sessaoValida } from '../../testing/sessoes';
import { Sessao } from './autenticacao.modelos';
import { autenticadoGuard, visitanteGuard } from './autenticacao.guard';

const rota = {} as ActivatedRouteSnapshot;
const estado = { url: '/painel' } as RouterStateSnapshot;

function iniciar(sessao: Sessao | null): Router {
  guardarSessao(sessao);
  TestBed.configureTestingModule({
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  return TestBed.inject(Router);
}

describe('autenticadoGuard', () => {
  it('deve liberar quem tem sessão válida', () => {
    iniciar(sessaoValida());

    const resultado = TestBed.runInInjectionContext(() => autenticadoGuard(rota, estado));

    expect(resultado).toBe(true);
  });

  it('deve mandar para o login guardando a rota pedida', () => {
    const roteador = iniciar(null);

    const resultado = TestBed.runInInjectionContext(() => autenticadoGuard(rota, estado));

    expect(roteador.serializeUrl(resultado as UrlTree)).toBe('/login?retorno=%2Fpainel');
  });

  it('deve tratar sessão expirada como visitante', () => {
    const roteador = iniciar(sessaoExpirada());

    const resultado = TestBed.runInInjectionContext(() => autenticadoGuard(rota, estado));

    expect(roteador.serializeUrl(resultado as UrlTree)).toBe('/login?retorno=%2Fpainel');
  });
});

describe('visitanteGuard', () => {
  it('deve liberar quem não tem sessão', () => {
    iniciar(null);

    const resultado = TestBed.runInInjectionContext(() => visitanteGuard(rota, estado));

    expect(resultado).toBe(true);
  });

  it('deve mandar para o painel quem já está autenticado', () => {
    const roteador = iniciar(sessaoValida());

    const resultado = TestBed.runInInjectionContext(() => visitanteGuard(rota, estado));

    expect(roteador.serializeUrl(resultado as UrlTree)).toBe('/painel');
  });
});
