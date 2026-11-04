import { Sessao } from './autenticacao.modelos';

export const CHAVE_SESSAO = 'financeflow.sessao';

export function lerSessaoSalva(): Sessao | null {
  try {
    const bruto = localStorage.getItem(CHAVE_SESSAO);
    if (bruto === null) {
      return null;
    }
    const dados: unknown = JSON.parse(bruto);
    return ehSessao(dados) ? dados : null;
  } catch {
    return null;
  }
}

export function salvarSessao(sessao: Sessao | null): void {
  try {
    if (sessao === null) {
      localStorage.removeItem(CHAVE_SESSAO);
    } else {
      localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
    }
  } catch {
    return;
  }
}

function ehSessao(dados: unknown): dados is Sessao {
  return (
    typeof dados === 'object' &&
    dados !== null &&
    typeof (dados as Sessao).tokenAcesso === 'string' &&
    typeof (dados as Sessao).expiraEm === 'string'
  );
}
