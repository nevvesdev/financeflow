import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, switchMap, tap } from 'rxjs';

import {
  CadastroRequisicao,
  LoginRequisicao,
  Sessao,
  TokenResposta,
  Usuario,
} from './autenticacao.modelos';
import { lerSessaoSalva, salvarSessao } from './armazenamento-sessao';

@Injectable({ providedIn: 'root' })
export class AutenticacaoService {
  private readonly http = inject(HttpClient);
  private readonly roteador = inject(Router);

  private readonly sessao = signal<Sessao | null>(lerSessaoSalva());
  private readonly usuarioAtual = signal<Usuario | null>(null);

  readonly usuario = this.usuarioAtual.asReadonly();

  sessaoValida(): boolean {
    const sessao = this.sessao();
    if (sessao === null) {
      return false;
    }
    if (Date.parse(sessao.expiraEm) <= Date.now()) {
      this.limparSessao();
      return false;
    }
    return true;
  }

  tokenValido(): string | null {
    const sessao = this.sessao();
    return sessao !== null && this.sessaoValida() ? sessao.tokenAcesso : null;
  }

  entrar(credenciais: LoginRequisicao): Observable<Usuario> {
    return this.http.post<TokenResposta>('/api/auth/login', credenciais).pipe(
      tap((resposta) => this.iniciarSessao(resposta)),
      switchMap(() => this.carregarUsuario()),
    );
  }

  cadastrar(dados: CadastroRequisicao): Observable<Usuario> {
    return this.http
      .post<Usuario>('/api/auth/cadastro', dados)
      .pipe(switchMap(() => this.entrar({ email: dados.email, senha: dados.senha })));
  }

  carregarUsuario(): Observable<Usuario> {
    return this.http
      .get<Usuario>('/api/auth/eu')
      .pipe(tap((usuario) => this.usuarioAtual.set(usuario)));
  }

  sair(): void {
    this.limparSessao();
    void this.roteador.navigate(['/login']);
  }

  encerrarPorExpiracao(): void {
    const retorno = this.roteador.url;
    this.limparSessao();
    const destinoValido = retorno !== '/' && !retorno.startsWith('/login');
    void this.roteador.navigate(['/login'], {
      queryParams: destinoValido ? { retorno } : {},
    });
  }

  private iniciarSessao(resposta: TokenResposta): void {
    const sessao: Sessao = { tokenAcesso: resposta.tokenAcesso, expiraEm: resposta.expiraEm };
    this.sessao.set(sessao);
    salvarSessao(sessao);
  }

  private limparSessao(): void {
    this.sessao.set(null);
    this.usuarioAtual.set(null);
    salvarSessao(null);
  }
}
