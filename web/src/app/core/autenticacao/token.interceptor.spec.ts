import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { describe, expect, it } from 'vitest';

import { guardarSessao, sessaoExpirada, sessaoValida } from '../../testing/sessoes';
import { Sessao } from './autenticacao.modelos';
import { tokenInterceptor } from './token.interceptor';

function iniciar(sessao: Sessao | null) {
  guardarSessao(sessao);
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(withInterceptors([tokenInterceptor])),
      provideHttpClientTesting(),
      provideRouter([]),
    ],
  });
  return { http: TestBed.inject(HttpClient), controle: TestBed.inject(HttpTestingController) };
}

describe('tokenInterceptor', () => {
  it('deve anexar o token às chamadas da api', () => {
    const { http, controle } = iniciar(sessaoValida());

    http.get('/api/painel').subscribe();

    const requisicao = controle.expectOne('/api/painel');
    expect(requisicao.request.headers.get('Authorization')).toBe('Bearer token-123');
    controle.verify();
  });

  it.each(['/api/auth/login', '/api/auth/cadastro'])(
    'não deve anexar o token à rota pública %s',
    (rota) => {
      const { http, controle } = iniciar(sessaoValida());

      http.post(rota, {}).subscribe();

      expect(controle.expectOne(rota).request.headers.has('Authorization')).toBe(false);
      controle.verify();
    },
  );

  it('não deve enviar o token para domínios externos', () => {
    const { http, controle } = iniciar(sessaoValida());

    http.get('https://exemplo.com/api/dados').subscribe();

    expect(
      controle.expectOne('https://exemplo.com/api/dados').request.headers.has('Authorization'),
    ).toBe(false);
    controle.verify();
  });

  it('não deve anexar nada sem sessão', () => {
    const { http, controle } = iniciar(null);

    http.get('/api/painel').subscribe();

    expect(controle.expectOne('/api/painel').request.headers.has('Authorization')).toBe(false);
    controle.verify();
  });

  it('não deve anexar um token expirado', () => {
    const { http, controle } = iniciar(sessaoExpirada());

    http.get('/api/painel').subscribe();

    expect(controle.expectOne('/api/painel').request.headers.has('Authorization')).toBe(false);
    controle.verify();
  });
});
