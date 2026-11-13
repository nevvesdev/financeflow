import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { Subscription, finalize } from 'rxjs';

import { descreverErro } from '../../core/api/erro-api';
import { AutenticacaoService } from '../../core/autenticacao/autenticacao.service';
import { ToastService } from '../../core/toast/toast.service';
import { descreverPrazo } from '../../shared/formatadores';
import { mesAtual, nomeDoMes } from '../../shared/mes';
import { DataCurtaPipe } from '../../shared/pipes/data-curta.pipe';
import { MoedaPipe } from '../../shared/pipes/moeda.pipe';
import { PercentualPipe } from '../../shared/pipes/percentual.pipe';
import { SeletorMes } from '../../shared/seletor-mes/seletor-mes';
import { LancamentoService } from '../lancamentos/lancamento.service';
import { ContaAVencer, Painel } from './painel.modelos';
import { PainelService } from './painel.service';

@Component({
  selector: 'app-pagina-painel',
  imports: [SeletorMes, MoedaPipe, PercentualPipe, DataCurtaPipe],
  templateUrl: './pagina-painel.html',
  styleUrl: './pagina-painel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginaPainel {
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly painelService = inject(PainelService);
  private readonly lancamentoService = inject(LancamentoService);
  private readonly toasts = inject(ToastService);

  private requisicao: Subscription | null = null;

  readonly mes = signal(mesAtual());
  readonly painel = signal<Painel | null>(null);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly pagando = signal<ReadonlySet<string>>(new Set());

  readonly primeiroNome = computed(() => this.autenticacao.usuario()?.nome.split(' ')[0] ?? '');
  readonly rotuloMes = computed(() => nomeDoMes(this.mes()));

  readonly totalDespesasPorNatureza = computed(() => {
    const dados = this.painel();
    return dados ? dados.despesasPorNatureza.fixas + dados.despesasPorNatureza.variaveis : 0;
  });

  readonly percentualFixas = computed(() => {
    const dados = this.painel();
    const total = this.totalDespesasPorNatureza();
    return dados && total > 0 ? (dados.despesasPorNatureza.fixas / total) * 100 : 0;
  });

  readonly percentualVariaveis = computed(() =>
    this.totalDespesasPorNatureza() > 0 ? 100 - this.percentualFixas() : 0,
  );

  constructor() {
    inject(DestroyRef).onDestroy(() => this.requisicao?.unsubscribe());
    this.carregar(false);
  }

  alterarMes(mes: string): void {
    this.mes.set(mes);
    this.carregar(false);
  }

  recarregar(): void {
    this.carregar(false);
  }

  marcarComoPaga(conta: ContaAVencer): void {
    if (this.pagando().has(conta.id)) {
      return;
    }
    this.pagando.update((atual) => new Set(atual).add(conta.id));

    this.lancamentoService
      .marcarComoPaga(conta.id)
      .pipe(
        finalize(() =>
          this.pagando.update((atual) => {
            const copia = new Set(atual);
            copia.delete(conta.id);
            return copia;
          }),
        ),
      )
      .subscribe({
        next: () => {
          this.toasts.sucesso(`${conta.descricao} marcada como paga`);
          this.carregar(true);
        },
        error: (erro: unknown) => this.toasts.erro(descreverErro(erro)),
      });
  }

  percentualRealizado(realizado: number, previsto: number): number {
    return previsto > 0 ? Math.min(100, (realizado / previsto) * 100) : 0;
  }

  prazo(conta: ContaAVencer): string {
    return descreverPrazo(conta.diasParaVencer);
  }

  private carregar(silencioso: boolean): void {
    this.requisicao?.unsubscribe();
    if (!silencioso) {
      this.carregando.set(true);
      this.erro.set(null);
    }

    this.requisicao = this.painelService.buscar(this.mes()).subscribe({
      next: (painel) => {
        this.painel.set(painel);
        this.carregando.set(false);
        this.erro.set(null);
      },
      error: (erro: unknown) => {
        this.carregando.set(false);
        if (silencioso) {
          this.toasts.erro(descreverErro(erro));
        } else {
          this.erro.set(descreverErro(erro));
        }
      },
    });
  }
}
