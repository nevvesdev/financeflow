import { HttpErrorResponse } from '@angular/common/http';

export interface ProblemaApi {
  title?: string;
  status?: number;
  detail?: string;
  erros?: Record<string, string>;
}

const MENSAGEM_SEM_CONEXAO =
  'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.';
const MENSAGEM_PADRAO = 'Algo deu errado. Tente novamente em instantes.';

export function problemaDe(erro: unknown): ProblemaApi | null {
  if (erro instanceof HttpErrorResponse && typeof erro.error === 'object' && erro.error !== null) {
    return erro.error as ProblemaApi;
  }
  return null;
}

export function descreverErro(erro: unknown): string {
  if (erro instanceof HttpErrorResponse && erro.status === 0) {
    return MENSAGEM_SEM_CONEXAO;
  }
  return problemaDe(erro)?.detail ?? MENSAGEM_PADRAO;
}

export function errosPorCampo(erro: unknown): Record<string, string> {
  return problemaDe(erro)?.erros ?? {};
}
