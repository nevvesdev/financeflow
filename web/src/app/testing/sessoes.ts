import { CHAVE_SESSAO } from '../core/autenticacao/armazenamento-sessao';
import { Sessao } from '../core/autenticacao/autenticacao.modelos';

const UMA_HORA = 3_600_000;

export function sessaoValida(): Sessao {
  return { tokenAcesso: 'token-123', expiraEm: new Date(Date.now() + UMA_HORA).toISOString() };
}

export function sessaoExpirada(): Sessao {
  return { tokenAcesso: 'token-velho', expiraEm: new Date(Date.now() - UMA_HORA).toISOString() };
}

export function guardarSessao(sessao: Sessao | null): void {
  localStorage.clear();
  if (sessao !== null) {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
  }
}
