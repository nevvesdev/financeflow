import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  Injector,
  afterNextRender,
  computed,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';

import { proximoIndice, valorDoEvento } from '../eventos';
import { PRESETS, Periodo, PresetPeriodo, calcularPeriodo, rotuloDoPeriodo } from '../periodo';

@Component({
  selector: 'app-seletor-periodo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="periodo">
      <button
        #gatilho
        class="botao botao--secundario periodo__gatilho"
        type="button"
        (click)="alternar()"
        aria-haspopup="dialog"
        [attr.aria-expanded]="aberto()"
      >
        <svg class="periodo__icone" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
          <path
            d="M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z"
          />
        </svg>
        <span>{{ rotulo() }}</span>
      </button>

      @if (aberto()) {
        <div class="periodo__popover" role="dialog" aria-label="Selecionar período">
          <div #lista class="periodo__lista" role="group" aria-label="Atalhos" (keydown)="aoTeclarNasOpcoes($event)">
            @for (opcao of presets; track opcao.id) {
              <button
                class="periodo__opcao"
                type="button"
                [attr.aria-pressed]="opcao.id === presetAtivo()"
                (click)="escolher(opcao.id)"
              >
                {{ opcao.rotulo }}
              </button>
            }
          </div>

          <div class="periodo__personalizado">
            <p class="periodo__titulo">Intervalo personalizado</p>
            <div class="periodo__datas">
              <label class="periodo__campo">
                <span>De</span>
                <input type="date" [value]="inicioPersonalizado()" (input)="definirInicio($event)" />
              </label>
              <label class="periodo__campo">
                <span>Até</span>
                <input type="date" [value]="fimPersonalizado()" (input)="definirFim($event)" />
              </label>
            </div>
            <button
              class="botao botao--primario botao--cheio"
              type="button"
              (click)="aplicarPersonalizado()"
              [disabled]="!personalizadoValido()"
            >
              Aplicar intervalo
            </button>
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    .periodo {
      position: relative;
      display: inline-block;
    }

    .periodo__icone {
      width: 1.1rem;
      height: 1.1rem;
      fill: none;
      stroke: currentColor;
      stroke-width: 1.8;
      stroke-linecap: round;
      stroke-linejoin: round;
    }

    .periodo__popover {
      position: absolute;
      top: calc(100% + var(--espaco-2));
      right: 0;
      z-index: 20;
      display: grid;
      gap: var(--espaco-4);
      width: 19rem;
      padding: var(--espaco-4);
      border: 1px solid var(--cor-borda-forte);
      border-radius: var(--raio-cartao);
      background: var(--cor-superficie-realce);
      box-shadow: var(--sombra-cartao);
      animation: popover-entrada var(--movimento-medio) var(--curva-padrao) both;
    }

    .periodo__lista {
      display: grid;
      gap: var(--espaco-1);
    }

    .periodo__opcao {
      padding: 0.55rem 0.75rem;
      border: 0;
      border-radius: 10px;
      background: transparent;
      color: var(--cor-texto-suave);
      font-size: var(--texto-sm);
      font-weight: 600;
      text-align: left;
      cursor: pointer;
      transition:
        background-color var(--movimento-rapido) var(--curva-padrao),
        color var(--movimento-rapido) var(--curva-padrao);
    }

    .periodo__opcao:hover {
      background: var(--cor-borda);
      color: var(--cor-texto);
    }

    .periodo__opcao[aria-pressed='true'] {
      background: var(--cor-destaque-suave);
      color: var(--cor-destaque-texto);
    }

    .periodo__personalizado {
      display: grid;
      gap: var(--espaco-3);
      padding-top: var(--espaco-4);
      border-top: 1px solid var(--cor-borda);
    }

    .periodo__titulo {
      color: var(--cor-texto-suave);
      font-size: var(--texto-xs);
      font-weight: 600;
      letter-spacing: 0.04em;
      text-transform: uppercase;
    }

    .periodo__datas {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: var(--espaco-2);
    }

    .periodo__campo {
      display: grid;
      gap: var(--espaco-1);
      font-size: var(--texto-xs);
      font-weight: 600;
    }

    .periodo__campo input {
      width: 100%;
      min-height: 2.5rem;
      padding: 0.4rem 0.5rem;
      border: 1px solid var(--cor-borda-forte);
      border-radius: 10px;
      background: var(--cor-superficie);
      color: var(--cor-texto);
      font: inherit;
      font-size: var(--texto-sm);
    }

    @keyframes popover-entrada {
      from {
        opacity: 0;
        transform: translateY(-6px) scale(0.98);
      }

      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }
  `,
})
export class SeletorPeriodo {
  private readonly elemento = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injetor = inject(Injector);
  private readonly gatilho = viewChild<ElementRef<HTMLButtonElement>>('gatilho');
  private readonly lista = viewChild<ElementRef<HTMLElement>>('lista');

  readonly periodo = input.required<Periodo>();
  readonly periodoAlterado = output<Periodo>();

  readonly presets = PRESETS;
  readonly aberto = signal(false);
  readonly inicioPersonalizado = signal('');
  readonly fimPersonalizado = signal('');

  readonly rotulo = computed(() => rotuloDoPeriodo(this.periodo()));
  readonly presetAtivo = computed<PresetPeriodo | null>(
    () =>
      PRESETS.find(
        (opcao) =>
          calcularPeriodo(opcao.id).inicio === this.periodo().inicio &&
          calcularPeriodo(opcao.id).fim === this.periodo().fim,
      )?.id ?? null,
  );
  readonly personalizadoValido = computed(() => {
    const inicio = this.inicioPersonalizado();
    const fim = this.fimPersonalizado();
    return inicio !== '' && fim !== '' && inicio <= fim;
  });

  alternar(): void {
    if (this.aberto()) {
      this.fechar(true);
      return;
    }
    this.inicioPersonalizado.set(this.periodo().inicio ?? '');
    this.fimPersonalizado.set(this.periodo().fim ?? '');
    this.aberto.set(true);
    afterNextRender(
      () => this.lista()?.nativeElement.querySelector<HTMLButtonElement>('button')?.focus(),
      { injector: this.injetor },
    );
  }

  escolher(preset: PresetPeriodo): void {
    this.periodoAlterado.emit(calcularPeriodo(preset));
    this.fechar(true);
  }

  aplicarPersonalizado(): void {
    if (!this.personalizadoValido()) {
      return;
    }
    this.periodoAlterado.emit({
      inicio: this.inicioPersonalizado(),
      fim: this.fimPersonalizado(),
    });
    this.fechar(true);
  }

  definirInicio(evento: Event): void {
    this.inicioPersonalizado.set(valorDoEvento(evento));
  }

  definirFim(evento: Event): void {
    this.fimPersonalizado.set(valorDoEvento(evento));
  }

  aoTeclarNasOpcoes(evento: KeyboardEvent): void {
    const botoes = Array.from(
      this.lista()?.nativeElement.querySelectorAll<HTMLButtonElement>('button') ?? [],
    );
    const atual = botoes.indexOf(document.activeElement as HTMLButtonElement);
    const destino = proximoIndice(Math.max(atual, 0), botoes.length, evento.key);
    if (destino === null || botoes.length === 0) {
      return;
    }
    evento.preventDefault();
    botoes[destino].focus();
  }

  @HostListener('document:keydown.escape')
  aoPressionarEscape(): void {
    if (this.aberto()) {
      this.fechar(true);
    }
  }

  @HostListener('document:click', ['$event'])
  aoClicarFora(evento: MouseEvent): void {
    if (this.aberto() && !this.elemento.nativeElement.contains(evento.target as Node)) {
      this.fechar(false);
    }
  }

  private fechar(devolverFoco: boolean): void {
    this.aberto.set(false);
    if (devolverFoco) {
      this.gatilho()?.nativeElement.focus();
    }
  }
}