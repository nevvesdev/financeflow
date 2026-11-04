import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { describe, expect, it, vi } from 'vitest';

import { guardarSessao, sessaoExpirada, sessaoValida } from '../../testing/sessoes';
import { CHAVE_SESSAO } from './armazenamento-sessao';
import { Sessao, TokenResposta, Usuario } from './autenticacao.modelos';
import { AutenticacaoService } from './autenticacao.service';

const usuarioMaria: Usuario = { id: 'u-1', nome: 'Maria Silva', email: 'maria@financeflow.dev' };

function tokenResposta(sessao: Sessao): TokenResposta {
  return { tokenAcesso: sessao.tokenAcesso, tipo: 'Bearer', expiraEm: sessao.expiraEm };
}

function iniciar(sessaoSalva: Sessao | null = null) {
  guardarSessao(sessaoSalva);
  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
  });
  return {
    servico: TestBed.inject(AutenticacaoService),
    http: TestBed.inject(HttpTestingController),
    roteador: TestBed.inject(Router),
  };
}

describe('AutenticacaoService', () => {
  it('deve autenticar, guardar a sessão e carregar o usuário', () => {
    const { servico, http } = iniciar();
    const sessao = sessaoValida();

    servico.entrar({ email: 'maria@financeflow.dev', senha: 'senhaSegura123' }).subscribe();

    const login = http.expectOne('/api/auth/login');
    expect(login.request.method).toBe('POST');
    login.flush(tokenResposta(sessao));
    http.expectOne('/api/auth/eu').flush(usuarioMaria);

    expect(servico.sessaoValida()).toBe(true);
    expect(servico.tokenValido()).toBe('token-123');
    expect(servico.usuario()).toEqual(usuarioMaria);
    expect(localStorage.getItem(CHAVE_SESSAO)).not.toBeNull();
    http.verify();
  });

  it('deve cadastrar e entrar em seguida com as mesmas credenciais', () => {
    const { servico, http } = iniciar();

    servico
      .cadastrar({ nome: 'Maria Silva', email: 'maria@financeflow.dev', senha: 'senhaSegura123' })
      .subscribe();

    http.expectOne('/api/auth/cadastro').flush(usuarioMaria);
    const login = http.expectOne('/api/auth/login');
    expect(login.request.body).toEqual({ email: 'maria@financeflow.dev', senha: 'senhaSegura123' });
    login.flush(tokenResposta(sessaoValida()));
    http.expectOne('/api/auth/eu').flush(usuarioMaria);

    expect(servico.sessaoValida()).toBe(true);
    http.verify();
  });

  it('deve restaurar uma sessão válida guardada no navegador', () => {
    const { servico } = iniciar(sessaoValida());

    expect(servico.sessaoValida()).toBe(true);
    expect(servico.tokenValido()).toBe('token-123');
  });

  it('deve descartar uma sessão expirada', () => {
    const { servico } = iniciar(sessaoExpirada());

    expect(servico.sessaoValida()).toBe(false);
    expect(servico.tokenValido()).toBeNull();
    expect(localStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });

  it('deve ignorar dados corrompidos no armazenamento', () => {
    guardarSessao(null);
    localStorage.setItem(CHAVE_SESSAO, 'isso não é json');
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });

    expect(TestBed.inject(AutenticacaoService).sessaoValida()).toBe(false);
  });

  it('deve limpar a sessão e ir para o login ao sair', () => {
    const { servico, roteador } = iniciar(sessaoValida());
    const navegar = vi.spyOn(roteador, 'navigate').mockResolvedValue(true);

    servico.sair();

    expect(servico.sessaoValida()).toBe(false);
    expect(localStorage.getItem(CHAVE_SESSAO)).toBeNull();
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });

  it('deve guardar a rota atual como retorno ao encerrar por expiração', async () => {
    guardarSessao(sessaoValida());
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'painel', children: [] }]),
      ],
    });
    const servico = TestBed.inject(AutenticacaoService);
    const roteador = TestBed.inject(Router);
    await roteador.navigateByUrl('/painel');
    const navegar = vi.spyOn(roteador, 'navigate').mockResolvedValue(true);

    servico.encerrarPorExpiracao();

    expect(navegar).toHaveBeenCalledWith(['/login'], { queryParams: { retorno: '/painel' } });
    expect(servico.sessaoValida()).toBe(false);
  });
});
