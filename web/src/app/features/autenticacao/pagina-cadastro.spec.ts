import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { Usuario } from '../../core/autenticacao/autenticacao.modelos';
import { AutenticacaoService } from '../../core/autenticacao/autenticacao.service';
import { PaginaCadastro } from './pagina-cadastro';

const usuario: Usuario = { id: 'u-1', nome: 'Maria Silva', email: 'maria@financeflow.dev' };
const dados = {
  nome: 'Maria Silva',
  email: 'maria@financeflow.dev',
  senha: 'senhaSegura123',
  confirmacao: 'senhaSegura123',
};

describe('PaginaCadastro', () => {
  const autenticacao = { cadastrar: vi.fn() };

  beforeEach(() => {
    autenticacao.cadastrar.mockReset();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AutenticacaoService, useValue: autenticacao }],
    });
  });

  it('não deve enviar com o formulário inválido', () => {
    const componente = TestBed.createComponent(PaginaCadastro).componentInstance;

    componente.cadastrar();

    expect(autenticacao.cadastrar).not.toHaveBeenCalled();
    expect(componente.mensagemDoCampo('nome')).toBe('Campo obrigatório.');
  });

  it('deve exigir senha com pelo menos oito caracteres', () => {
    const componente = TestBed.createComponent(PaginaCadastro).componentInstance;
    componente.formulario.setValue({ ...dados, senha: '123', confirmacao: '123' });

    componente.cadastrar();

    expect(componente.mensagemDoCampo('senha')).toBe('A senha deve ter pelo menos 8 caracteres.');
  });

  it('deve exigir que a confirmação seja igual à senha', () => {
    const componente = TestBed.createComponent(PaginaCadastro).componentInstance;
    componente.formulario.setValue({ ...dados, confirmacao: 'outraSenha123' });

    componente.cadastrar();

    expect(autenticacao.cadastrar).not.toHaveBeenCalled();
    expect(componente.mensagemDoCampo('confirmacao')).toBe('As senhas não conferem.');
  });

  it('deve cadastrar sem enviar a confirmação e ir para o painel', () => {
    autenticacao.cadastrar.mockReturnValue(of(usuario));
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const componente = TestBed.createComponent(PaginaCadastro).componentInstance;
    componente.formulario.setValue(dados);

    componente.cadastrar();

    expect(autenticacao.cadastrar).toHaveBeenCalledWith({
      nome: 'Maria Silva',
      email: 'maria@financeflow.dev',
      senha: 'senhaSegura123',
    });
    expect(navegar).toHaveBeenCalledWith('/painel');
  });

  it('deve mostrar o erro do servidor no campo correspondente', () => {
    autenticacao.cadastrar.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { erros: { email: 'deve ser um endereço de e-mail válido' } },
          }),
      ),
    );
    const componente = TestBed.createComponent(PaginaCadastro).componentInstance;
    componente.formulario.setValue(dados);

    componente.cadastrar();

    expect(componente.mensagemDoCampo('email')).toBe('deve ser um endereço de e-mail válido');
    expect(componente.erroGeral()).toBeNull();
    expect(componente.enviando()).toBe(false);
  });

  it('deve mostrar a mensagem geral quando o e-mail já existe', () => {
    autenticacao.cadastrar.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { detail: 'Já existe uma conta com este e-mail' },
          }),
      ),
    );
    const componente = TestBed.createComponent(PaginaCadastro).componentInstance;
    componente.formulario.setValue(dados);

    componente.cadastrar();

    expect(componente.erroGeral()).toBe('Já existe uma conta com este e-mail');
  });
});
