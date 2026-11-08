import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';

export type Tema = 'escuro' | 'claro';

export const CHAVE_TEMA = 'financeflow.tema';

@Injectable({ providedIn: 'root' })
export class TemaService {
  private readonly documento = inject(DOCUMENT);
  private readonly temaAtual = signal<Tema>(lerTemaSalvo());

  readonly tema = this.temaAtual.asReadonly();

  constructor() {
    this.aplicar(this.temaAtual());
  }

  alternar(): void {
    const proximo: Tema = this.temaAtual() === 'escuro' ? 'claro' : 'escuro';
    this.temaAtual.set(proximo);
    salvarTema(proximo);

    if (this.podeAnimar()) {
      this.documento.startViewTransition(() => this.aplicar(proximo));
    } else {
      this.aplicar(proximo);
    }
  }

  private aplicar(tema: Tema): void {
    this.documento.documentElement.dataset['tema'] = tema;
  }

  private podeAnimar(): boolean {
    const movimentoReduzido =
      this.documento.defaultView?.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches ?? false;
    return typeof this.documento.startViewTransition === 'function' && !movimentoReduzido;
  }
}

function lerTemaSalvo(): Tema {
  try {
    return localStorage.getItem(CHAVE_TEMA) === 'claro' ? 'claro' : 'escuro';
  } catch {
    return 'escuro';
  }
}

function salvarTema(tema: Tema): void {
  try {
    localStorage.setItem(CHAVE_TEMA, tema);
  } catch {
    return;
  }
}
