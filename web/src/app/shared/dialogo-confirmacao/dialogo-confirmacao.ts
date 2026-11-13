import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  input,
  output,
  viewChild,
} from '@angular/core';

let proximoId = 0;

@Component({
  selector: 'app-dialogo-confirmacao',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <dialog #dialogo class="dialogo" [attr.aria-labelledby]="idTitulo" (click)="aoClicarNoFundo($event)">
      <div class="dialogo__conteudo">
        <h2 class="dialogo__titulo" [id]="idTitulo">{{ titulo() }}</h2>
        <p class="dialogo__mensagem">{{ mensagem() }}</p>
        <div class="dialogo__acoes">
          <button class="botao botao--secundario" type="button" (click)="fechar()">Cancelar</button>
          <button
            class="botao"
            type="button"
            [class.botao--primario]="!perigo()"
            [class.dialogo__perigo]="perigo()"
            (click)="confirmar()"
          >
            {{ rotuloConfirmar() }}
          </button>
        </div>
      </div>
    </dialog>
  `,
  styles: `
    .dialogo {
      width: min(26rem, calc(100vw - 2rem));
      padding: 0;
      border: 1px solid var(--cor-borda-forte);
      border-radius: var(--raio-cartao);
      background: var(--cor-superficie-realce);
      color: var(--cor-texto);
      box-shadow: var(--sombra-cartao);
    }

    .dialogo[open] {
      animation: dialogo-entrada var(--movimento-medio) var(--curva-padrao) both;
    }

    .dialogo::backdrop {
      background: rgb(0 0 0 / 55%);
      backdrop-filter: blur(2px);
    }

    .dialogo__conteudo {
      display: grid;
      gap: var(--espaco-4);
      padding: var(--espaco-5);
    }

    .dialogo__titulo {
      font-size: var(--texto-lg);
      font-weight: 650;
    }

    .dialogo__mensagem {
      color: var(--cor-texto-suave);
    }

    .dialogo__acoes {
      display: flex;
      justify-content: flex-end;
      gap: var(--espaco-2);
    }

    .dialogo__perigo {
      background: var(--cor-despesa);
      color: #1a0b0e;
    }

    .dialogo__perigo:hover:not(:disabled) {
      filter: brightness(1.08);
    }

    @keyframes dialogo-entrada {
      from {
        opacity: 0;
        transform: translateY(8px) scale(0.98);
      }

      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }
  `,
})
export class DialogoConfirmacao {
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  readonly idTitulo = `dialogo-titulo-${proximoId++}`;

  readonly titulo = input.required<string>();
  readonly mensagem = input.required<string>();
  readonly rotuloConfirmar = input('Confirmar');
  readonly perigo = input(false);
  readonly confirmado = output<void>();

  abrir(): void {
    const elemento = this.dialogo().nativeElement;
    if (elemento.open) {
      return;
    }
    if (typeof elemento.showModal === 'function') {
      elemento.showModal();
    } else {
      elemento.setAttribute('open', '');
    }
  }

  fechar(): void {
    const elemento = this.dialogo().nativeElement;
    if (typeof elemento.close === 'function') {
      elemento.close();
    } else {
      elemento.removeAttribute('open');
    }
  }

  confirmar(): void {
    this.confirmado.emit();
    this.fechar();
  }

  aoClicarNoFundo(evento: MouseEvent): void {
    if (evento.target === this.dialogo().nativeElement) {
      this.fechar();
    }
  }
}