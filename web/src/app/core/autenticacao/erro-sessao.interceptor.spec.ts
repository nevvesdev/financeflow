import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { describe, expect, it, vi } from 'vitest';

import { guardarSessao, sessaoValida } from '../../testing/sessoes';
import { AutenticacaoService } from './autenticacao.service';
import { erroSessaoInterceptor } from './erro-sessao.interceptor';

function iniciar() {
  guardarSessao(sessaoValida());
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(withInterceptors([erroSessaoInterceptor])),
      provideHttpClientTesting(),
      provideRouter([]),
    ],
  });
  const encerrar = vi
    .spyOn(TestBed.inject(AutenticacaoService), 'encerrarPorExpiracao')
    .mockImplementation(() => undefined);
  return {
    http: TestBed.inject(HttpClient),
    controle: TestBed.inject(HttpTestingController),
    encerrar,
  };
}

describe('erroSessaoInterceptor', () => {
  it('deve encerrar a sessão quando a api responde 401', () => {
    const { http, controle, encerrar } = iniciar();

    http.get('/api/painel').subscribe({ error: () => undefined });
    controle.expectOne('/api/painel').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(encerrar).toHaveBeenCalledOnce();
  });

  it('não deve encerrar a sessão quando o login falha com 401', () => {
    const { http, controle, encerrar } = iniciar();

    http.post('/api/auth/login', {}).subscribe({ error: () => undefined });
    controle
      .expectOne('/api/auth/login')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(encerrar).not.toHaveBeenCalled();
  });

  it('não deve encerrar a sessão em erros que não são 401', () => {
    const { http, controle, encerrar } = iniciar();

    http.get('/api/painel').subscribe({ error: () => undefined });
    controle.expectOne('/api/painel').flush(null, { status: 500, statusText: 'Server Error' });

    expect(encerrar).not.toHaveBeenCalled();
  });
});
