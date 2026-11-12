import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-marca',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="marca">
      <svg
        class="marca__icone"
        viewBox="0 0 32 32"
        width="32"
        height="32"
        aria-hidden="true"
        focusable="false"
      >
        <rect width="32" height="32" rx="9" fill="currentColor" />
        <path class="marca__linha" d="M8 21l5-6 4 3 7-9" />
      </svg>
      <span class="marca__nome">FinanceFlow</span>
    </span>
  `,
  styles: `
    .marca {
      display: inline-flex;
      align-items: center;
      gap: 0.6rem;
      color: var(--cor-primaria);
    }

    .marca__linha {
      fill: none;
      stroke: var(--cor-sobre-primaria);
      stroke-width: 2.6;
      stroke-linecap: round;
      stroke-linejoin: round;
    }

    .marca__nome {
      color: var(--cor-texto);
      font-size: 1.15rem;
      font-weight: 700;
      letter-spacing: -0.01em;
    }
  `,
})
export class Marca {}
