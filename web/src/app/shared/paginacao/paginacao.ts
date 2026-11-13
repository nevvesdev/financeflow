import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

export function janelaDePaginas(atual: number, total: number): (number | null)[] {
  if (total <= 7) {
    return Array.from({ length: total }, (_, indice) => indice);
  }

  const paginas = new Set<number>([0, total - 1, atual - 1, atual, atual + 1]);
  if (atual <= 2) {
    for (let pagina = 0; pagina <= 4; pagina++) {
      paginas.add(pagina);
    }
  }
  if (atual >= total - 3) {
    for (let pagina = total - 5; pagina < total; pagina++) {
      paginas.add(pagina);
    }
  }

  const ordenadas = [...paginas].filter((pagina) => pagina >= 0 && pagina < total).sort((a, b) => a - b);
  const janela: (number | null)[] = [];
  ordenadas.forEach((pagina, indice) => {
    if (indice > 0) {
      const lacuna = pagina - ordenadas[indice - 1];
      if (lacuna === 2) {
        janela.push(pagina - 1);
      } else if (lacuna > 2) {
        janela.push(null);
      }
    }
    janela.push(pagina);
  });
  return janela;
}

@Component({
  selector: 'app-paginacao',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (totalPaginas() > 1) {
      <nav class="paginacao" aria-label="Paginação">
        <button
          class="botao botao--fantasma botao--icone"
          type="button"
          (click)="anterior()"
          [disabled]="pagina() === 0"
          aria-label="Página anterior"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" focusable="false">
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </button>

        <div class="paginacao__lista" role="list" [style.--indice]="indiceAtivo()">
          <span class="paginacao__pilula" aria-hidden="true"></span>
          @for (item of itens(); track $index) {
            <div class="paginacao__celula" role="listitem">
              @if (item === null) {
                <span class="paginacao__reticencias" aria-hidden="true">…</span>
              } @else {
                <button
                  class="paginacao__pagina"
                  type="button"
                  [attr.aria-current]="item === pagina() ? 'page' : null"
                  [attr.aria-label]="'Página ' + (item + 1)"
                  (click)="irPara(item)"
                >
                  {{ item + 1 }}
                </button>
              }
            </div>
          }
        </div>

        <button
          class="botao botao--fantasma botao--icone"
          type="button"
          (click)="proxima()"
          [disabled]="pagina() >= totalPaginas() - 1"
          aria-label="Próxima página"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" focusable="false">
            <path d="M9 18l6-6-6-6" />
          </svg>
        </button>

        <p class="paginacao__resumo">Página {{ pagina() + 1 }} de {{ totalPaginas() }}</p>
      </nav>
    }
  `,
  styles: `
    .paginacao {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: var(--espaco-2);
    }

    .paginacao__lista {
      --celula: 2.25rem;
      --intervalo: 0.25rem;

      position: relative;
      display: grid;
      grid-auto-flow: column;
      grid-auto-columns: var(--celula);
      gap: var(--intervalo);
    }

    .paginacao__pilula {
      position: absolute;
      top: 0;
      left: 0;
      width: var(--celula);
      height: var(--celula);
      border-radius: var(--raio-controle);
      background: var(--cor-destaque);
      transform: translateX(calc(var(--indice) * (var(--celula) + var(--intervalo))));
      transition: transform var(--movimento-medio) var(--curva-mola);
    }

    .paginacao__celula {
      position: relative;
      display: grid;
      place-items: center;
      height: var(--celula);
    }

    .paginacao__pagina {
      width: 100%;
      height: 100%;
      border: 0;
      border-radius: var(--raio-controle);
      background: transparent;
      color: var(--cor-texto-suave);
      font-size: var(--texto-sm);
      font-weight: 600;
      cursor: pointer;
      transition: color var(--movimento-rapido) var(--curva-padrao);
    }

    .paginacao__pagina:hover {
      color: var(--cor-texto);
    }

    .paginacao__pagina[aria-current='page'] {
      color: var(--cor-sobre-destaque);
    }

    .paginacao__reticencias {
      color: var(--cor-texto-fraco);
    }

    .paginacao__resumo {
      margin-left: var(--espaco-2);
      color: var(--cor-texto-suave);
      font-size: var(--texto-sm);
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
export class Paginacao {
  readonly pagina = input.required<number>();
  readonly totalPaginas = input.required<number>();
  readonly paginaAlterada = output<number>();

  readonly itens = computed(() => janelaDePaginas(this.pagina(), this.totalPaginas()));
  readonly indiceAtivo = computed(() => this.itens().indexOf(this.pagina()));

  irPara(pagina: number): void {
    if (pagina !== this.pagina() && pagina >= 0 && pagina < this.totalPaginas()) {
      this.paginaAlterada.emit(pagina);
    }
  }

  anterior(): void {
    this.irPara(this.pagina() - 1);
  }

  proxima(): void {
    this.irPara(this.pagina() + 1);
  }
}