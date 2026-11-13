import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

import { mesAtual, nomeDoMes, somarMeses } from '../mes';

@Component({
  selector: 'app-seletor-mes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="seletor" role="group" aria-label="Selecionar mês">
      <button
        class="botao botao--fantasma botao--icone"
        type="button"
        (click)="anterior()"
        aria-label="Mês anterior"
      >
        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" focusable="false">
          <path d="M15 18l-6-6 6-6" />
        </svg>
      </button>
      <span class="seletor__rotulo" aria-live="polite">{{ rotulo() }}</span>
      <button
        class="botao botao--fantasma botao--icone"
        type="button"
        (click)="proximo()"
        aria-label="Próximo mês"
      >
        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" focusable="false">
          <path d="M9 18l6-6-6-6" />
        </svg>
      </button>
      @if (!ehMesAtual()) {
        <button class="botao botao--secundario" type="button" (click)="irParaHoje()">
          Mês atual
        </button>
      }
    </div>
  `,
  styles: `
    .seletor {
      display: inline-flex;
      align-items: center;
      gap: var(--espaco-1);
      padding: var(--espaco-1);
      border: 1px solid var(--cor-borda-forte);
      border-radius: var(--raio-controle);
      background: var(--cor-superficie);
    }

    .seletor__rotulo {
      min-width: 10.5rem;
      font-size: var(--texto-sm);
      font-weight: 600;
      text-align: center;
    }

    svg {
      fill: none;
      stroke: currentColor;
      stroke-width: 1.8;
      stroke-linecap: round;
      stroke-linejoin: round;
    }
  `,
})
export class SeletorMes {
  readonly mes = input.required<string>();
  readonly mesAlterado = output<string>();

  readonly rotulo = computed(() => nomeDoMes(this.mes()));
  readonly ehMesAtual = computed(() => this.mes() === mesAtual());

  anterior(): void {
    this.mesAlterado.emit(somarMeses(this.mes(), -1));
  }

  proximo(): void {
    this.mesAlterado.emit(somarMeses(this.mes(), 1));
  }

  irParaHoje(): void {
    this.mesAlterado.emit(mesAtual());
  }
}
