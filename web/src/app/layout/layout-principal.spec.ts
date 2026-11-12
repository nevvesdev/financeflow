import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { Usuario } from '../core/autenticacao/autenticacao.modelos';
import { AutenticacaoService } from '../core/autenticacao/autenticacao.service';
import { LayoutPrincipal } from './layout-principal';

const usuario: Usuario = { id: 'u-1', nome: 'Maria Silva', email: 'maria@financeflow.dev' };

describe('LayoutPrincipal', () => {
  const autenticacao = {
    usuario: () => null,
    carregarUsuario: vi.fn(),
    sair: vi.fn(),
  };

  beforeEach(() => {
    autenticacao.carregarUsuario.mockReset();
    autenticacao.carregarUsuario.mockReturnValue(of(usuario));
    autenticacao.sair.mockReset();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'painel', children: [] },
          { path: 'outra', children: [] },
        ]),
        { provide: AutenticacaoService, useValue: autenticacao },
      ],
    });
  });

  it('deve carregar o usuário quando ainda não foi carregado', () => {
    TestBed.createComponent(LayoutPrincipal);

    expect(autenticacao.carregarUsuario).toHaveBeenCalledOnce();
  });

  it('deve marcar o item do painel como ativo', async () => {
    const componente = TestBed.createComponent(LayoutPrincipal).componentInstance;

    await TestBed.inject(Router).navigateByUrl('/painel');

    expect(componente.indiceAtivo()).toBe(0);
  });

  it('não deve marcar item algum em rotas fora do menu', async () => {
    const componente = TestBed.createComponent(LayoutPrincipal).componentInstance;

    await TestBed.inject(Router).navigateByUrl('/outra');

    expect(componente.indiceAtivo()).toBe(-1);
  });

  it('deve recolher e expandir o menu', () => {
    const componente = TestBed.createComponent(LayoutPrincipal).componentInstance;

    expect(componente.recolhido()).toBe(false);
    componente.alternarMenu();
    expect(componente.recolhido()).toBe(true);
    componente.alternarMenu();
    expect(componente.recolhido()).toBe(false);
  });

  it('deve encerrar a sessão ao sair', () => {
    TestBed.createComponent(LayoutPrincipal).componentInstance.sair();

    expect(autenticacao.sair).toHaveBeenCalledOnce();
  });
});
