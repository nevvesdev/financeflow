import { Injectable, signal } from '@angular/core';

export type TipoToast = 'sucesso' | 'erro' | 'info';

export interface Toast {
  id: number;
  tipo: TipoToast;
  mensagem: string;
}

const DURACAO_PADRAO_MS = 4000;
const DURACAO_ERRO_MS = 7000;
const LIMITE_VISIVEL = 4;

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly lista = signal<readonly Toast[]>([]);
  private proximoId = 1;

  readonly toasts = this.lista.asReadonly();

  sucesso(mensagem: string): void {
    this.exibir('sucesso', mensagem, DURACAO_PADRAO_MS);
  }

  erro(mensagem: string): void {
    this.exibir('erro', mensagem, DURACAO_ERRO_MS);
  }

  info(mensagem: string): void {
    this.exibir('info', mensagem, DURACAO_PADRAO_MS);
  }

  fechar(id: number): void {
    this.lista.update((atual) => atual.filter((toast) => toast.id !== id));
  }

  private exibir(tipo: TipoToast, mensagem: string, duracaoMs: number): void {
    const id = this.proximoId++;
    this.lista.update((atual) => [...atual, { id, tipo, mensagem }].slice(-LIMITE_VISIVEL));
    setTimeout(() => this.fechar(id), duracaoMs);
  }
}
