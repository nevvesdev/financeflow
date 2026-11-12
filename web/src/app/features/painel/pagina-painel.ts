import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { AutenticacaoService } from '../../core/autenticacao/autenticacao.service';

@Component({
  selector: 'app-pagina-painel',
  templateUrl: './pagina-painel.html',
  styleUrl: './pagina-painel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginaPainel {
  private readonly autenticacao = inject(AutenticacaoService);

  readonly estatisticas = ['Saldo do mês', 'Entradas', 'Saídas'] as const;
  readonly primeiroNome = computed(() => this.autenticacao.usuario()?.nome.split(' ')[0] ?? '');
}
