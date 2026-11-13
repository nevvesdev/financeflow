import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  inject,
  input,
  output,
} from '@angular/core';

import { proximoIndice } from '../eventos';

export interface ItemAba {
  valor: string;
  rotulo: string;
}

const TECLAS_HORIZONTAIS = ['ArrowLeft', 'ArrowRight', 'Home', 'End'];

@Component({
  selector: 'app-abas',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="abas"
      role="tablist"
      [attr.aria-label]="rotulo()"
      [style.--indice]="indice()"
      [style.--total]="itens().length"
      (keydown)="aoTeclar($event)"
    >
      <span class="abas__indicador" aria-hidden="true"></span>
      @for (item of itens(); track item.valor) {
        <button
          class="abas__aba"
          type="button"
          role="tab"
          [id]="'aba-' + item.valor"
          [attr.aria-selected]="item.valor === selecionada()"
          [attr.aria-controls]="idPainel()"
          [attr.tabindex]="item.valor === selecionada() ? 0 : -1"
          (click)="selecionar.emit(item.valor)"
        >
          {{ item.rotulo }}
        </button>
      }
    </div>
  `,
  styles: `
    .abas {
      position: relative;
      display: inline-grid;
      grid-auto-flow: column;
      grid-auto-columns: 1fr;
      padding: var(--espaco-1);
      border: 1px solid var(--cor-borda-forte);
      border-radius: var(--raio-controle);
      background: var(--cor-superficie);
    }

    .abas__indicador {
      position: absolute;
      top: var(--espaco-1);
      bottom: var(--espaco-1);
      left: var(--espaco-1);
      width: calc((100% - 2 * var(--espaco-1)) / var(--total));
      border-radius: 9px;
      background: var(--cor-destaque-suave);
      transform: translateX(calc(var(--indice) * 100%));
      transition: transform var(--movimento-medio) var(--curva-mola);
    }

    .abas__aba {
      position: relative;
      padding: 0.45rem 1rem;
      border: 0;
      border-radius: 9px;
      background: transparent;
      color: var(--cor-texto-suave);
      font-size: var(--texto-sm);
      font-weight: 600;
      cursor: pointer;
      transition: color var(--movimento-rapido) var(--curva-padrao);
    }

    .abas__aba:hover {
      color: var(--cor-texto);
    }

    .abas__aba[aria-selected='true'] {
      color: var(--cor-destaque-texto);
    }
  `,
})
export class Abas {
  private readonly elemento = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly itens = input.required<readonly ItemAba[]>();
  readonly selecionada = input.required<string>();
  readonly idPainel = input.required<string>();
  readonly rotulo = input('Abas');
  readonly selecionar = output<string>();

  readonly indice = computed(() =>
    Math.max(
      0,
      this.itens().findIndex((item) => item.valor === this.selecionada()),
    ),
  );

  aoTeclar(evento: KeyboardEvent): void {
    if (!TECLAS_HORIZONTAIS.includes(evento.key)) {
      return;
    }
    const itens = this.itens();
    const destino = proximoIndice(this.indice(), itens.length, evento.key);
    if (destino === null) {
      return;
    }
    evento.preventDefault();
    this.selecionar.emit(itens[destino].valor);
    this.elemento.nativeElement.querySelectorAll<HTMLElement>('[role="tab"]')[destino]?.focus();
  }
}