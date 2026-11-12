import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { Usuario } from '../../core/autenticacao/autenticacao.modelos';
import { AutenticacaoService } from '../../core/autenticacao/autenticacao.service';
import { PaginaLogin } from './pagina-login';

const usuario: Usuario = { id: 'u-1', nome: 'Maria Silva', email: 'maria@financeflow.dev' };
const credenciais = { email: 'maria@financeflow.dev', senha: 'senhaSegura123' };

describe('PaginaLogin', () => {
  const autenticacao = { entrar: vi.fn() };

  beforeEach(() => {
    autenticacao.entrar.mockReset();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AutenticacaoService, useValue: autenticacao }],
    });
  });

  it('não deve enviar com o formulário inválido', () => {
    const componente = TestBed.createComponent(PaginaLogin).componentInstance;

    componente.entrar();

    expect(autenticacao.entrar).not.toHaveBeenCalled();
    expect(componente.formulario.controls.email.touched).toBe(true);
  });

  it('deve autenticar e ir para o painel', () => {
    autenticacao.entrar.mockReturnValue(of(usuario));
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const componente = TestBed.createComponent(PaginaLogin).componentInstance;
    componente.formulario.setValue(credenciais);

    componente.entrar();

    expect(autenticacao.entrar).toHaveBeenCalledWith(credenciais);
    expect(navegar).toHaveBeenCalledWith('/painel');
  });

  it('deve voltar para a rota pedida antes do login', () => {
    autenticacao.entrar.mockReturnValue(of(usuario));
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const fixture = TestBed.createComponent(PaginaLogin);
    fixture.componentRef.setInput('retorno', '/lancamentos');
    fixture.componentInstance.formulario.setValue(credenciais);

    fixture.componentInstance.entrar();

    expect(navegar).toHaveBeenCalledWith('/lancamentos');
  });

  it('deve ignorar um retorno que aponta para fora da aplicação', () => {
    autenticacao.entrar.mockReturnValue(of(usuario));
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const fixture = TestBed.createComponent(PaginaLogin);
    fixture.componentRef.setInput('retorno', '//malicioso.com');
    fixture.componentInstance.formulario.setValue(credenciais);

    fixture.componentInstance.entrar();

    expect(navegar).toHaveBeenCalledWith('/painel');
  });

  it('deve mostrar a mensagem da api quando o login falha', () => {
    autenticacao.entrar.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 401, error: { detail: 'Credenciais inválidas' } })),
    );
    const componente = TestBed.createComponent(PaginaLogin).componentInstance;
    componente.formulario.setValue(credenciais);

    componente.entrar();

    expect(componente.erroGeral()).toBe('Credenciais inválidas');
    expect(componente.enviando()).toBe(false);
  });
});
