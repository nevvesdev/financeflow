import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, Subscription, debounceTime, finalize } from 'rxjs';

import { NaturezaLancamento, TipoLancamento } from '../../core/api/dominio';
import { descreverErro } from '../../core/api/erro-api';
import { ToastService } from '../../core/toast/toast.service';
import { Abas, ItemAba } from '../../shared/abas/abas';
import { DialogoConfirmacao } from '../../shared/dialogo-confirmacao/dialogo-confirmacao';
import { valorDoEvento } from '../../shared/eventos';
import { formatarMoeda } from '../../shared/formatadores';
import { Paginacao } from '../../shared/paginacao/paginacao';
import { DataCurtaPipe } from '../../shared/pipes/data-curta.pipe';
import { MoedaPipe } from '../../shared/pipes/moeda.pipe';
import { Periodo, calcularPeriodo } from '../../shared/periodo';
import { SeletorPeriodo } from '../../shared/seletor-periodo/seletor-periodo';
import { Categoria } from '../categorias/categoria.modelos';
import { CategoriaService } from '../categorias/categoria.service';
import {
  CampoOrdenacao,
  FiltroLancamentos,
  Lancamento,
  Ordenacao,
  Pagina,
} from './lancamento.modelos';
import { LancamentoService } from './lancamento.service';
import { FormularioLancamento } from './formulario-lancamento';

type AbaTipo = 'TODOS' | TipoLancamento;
type Situacao = '' | 'ABERTOS' | 'PAGOS' | 'VENCIDOS';

const TAMANHO_PAGINA = 15;
const ESPERA_BUSCA_MS = 300;

@Component({
  selector: 'app-pagina-lancamentos',
  imports: [
    Abas,
    SeletorPeriodo,
    Paginacao,
    DialogoConfirmacao,
    FormularioLancamento,
    MoedaPipe,
    DataCurtaPipe,
  ],
  templateUrl: './pagina-lancamentos.html',
  styleUrl: './pagina-lancamentos.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginaLancamentos {
  private readonly lancamentoService = inject(LancamentoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly toasts = inject(ToastService);
  private readonly destruicao = inject(DestroyRef);
  private readonly busca$ = new Subject<string>();
  private readonly dialogoExclusao = viewChild(DialogoConfirmacao);
  private readonly formularioLancamento = viewChild(FormularioLancamento);

  private requisicao: Subscription | null = null;

  readonly abas: readonly ItemAba[] = [
    { valor: 'TODOS', rotulo: 'Todos' },
    { valor: 'RECEITA', rotulo: 'Receitas' },
    { valor: 'DESPESA', rotulo: 'Despesas' },
  ];

  readonly aba = signal<AbaTipo>('TODOS');
  readonly periodo = signal<Periodo>(calcularPeriodo('este-mes'));
  readonly buscaDigitada = signal('');
  readonly busca = signal('');
  readonly categoriaId = signal('');
  readonly natureza = signal<'' | NaturezaLancamento>('');
  readonly situacao = signal<Situacao>('');
  readonly ordenacao = signal<Ordenacao>({ campo: 'dataLancamento', direcao: 'desc' });
  readonly pagina = signal(0);

  readonly categorias = signal<readonly Categoria[]>([]);
  readonly resultado = signal<Pagina<Lancamento> | null>(null);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly processando = signal<ReadonlySet<string>>(new Set());
  readonly lancamentoParaExcluir = signal<Lancamento | null>(null);

  readonly categoriasDoFiltro = computed(() => {
    const aba = this.aba();
    return aba === 'TODOS' ? this.categorias() : this.categorias().filter((categoria) => categoria.tipo === aba);
  });

  readonly filtro = computed<FiltroLancamentos>(() => {
    const aba = this.aba();
    const natureza = this.natureza();
    const situacao = this.situacao();
    const periodo = this.periodo();
    return {
      tipo: aba === 'TODOS' ? undefined : aba,
      natureza: natureza === '' ? undefined : natureza,
      categoriaId: this.categoriaId() || undefined,
      paga: situacao === 'PAGOS' ? true : situacao === 'ABERTOS' ? false : undefined,
      vencidos: situacao === 'VENCIDOS' ? true : undefined,
      busca: this.busca().trim() || undefined,
      inicio: periodo.inicio ?? undefined,
      fim: periodo.fim ?? undefined,
    };
  });

  readonly temFiltros = computed(
    () =>
      this.aba() !== 'TODOS' ||
      this.busca().trim() !== '' ||
      this.categoriaId() !== '' ||
      this.natureza() !== '' ||
      this.situacao() !== '',
  );

  readonly mensagemExclusao = computed(() => {
    const lancamento = this.lancamentoParaExcluir();
    return lancamento
      ? `Excluir "${lancamento.descricao}" (${formatarMoeda(lancamento.valor)})? Esta ação não pode ser desfeita.`
      : '';
  });

  constructor() {
    this.destruicao.onDestroy(() => this.requisicao?.unsubscribe());

    this.busca$
      .pipe(debounceTime(ESPERA_BUSCA_MS), takeUntilDestroyed(this.destruicao))
      .subscribe((texto) => {
        this.busca.set(texto);
        this.reiniciar();
      });

    this.categoriaService
      .listar()
      .pipe(takeUntilDestroyed(this.destruicao))
      .subscribe({ next: (categorias) => this.categorias.set(categorias), error: () => undefined });

    this.carregar(false);
  }

  selecionarAba(valor: string): void {
    if (valor !== 'TODOS' && valor !== 'RECEITA' && valor !== 'DESPESA') {
      return;
    }
    this.aba.set(valor);
    const selecionada = this.categoriaId();
    if (selecionada !== '' && !this.categoriasDoFiltro().some((categoria) => categoria.id === selecionada)) {
      this.categoriaId.set('');
    }
    this.reiniciar();
  }

  alterarPeriodo(periodo: Periodo): void {
    this.periodo.set(periodo);
    this.reiniciar();
  }

  aoDigitar(evento: Event): void {
    const texto = valorDoEvento(evento);
    this.buscaDigitada.set(texto);
    this.busca$.next(texto);
  }

  alterarCategoria(evento: Event): void {
    this.categoriaId.set(valorDoEvento(evento));
    this.reiniciar();
  }

  alterarNatureza(evento: Event): void {
    const valor = valorDoEvento(evento);
    this.natureza.set(valor === 'FIXA' || valor === 'VARIAVEL' ? valor : '');
    this.reiniciar();
  }

  alterarSituacao(evento: Event): void {
    const valor = valorDoEvento(evento);
    this.situacao.set(
      valor === 'ABERTOS' || valor === 'PAGOS' || valor === 'VENCIDOS' ? valor : '',
    );
    this.reiniciar();
  }

  limparFiltros(): void {
    this.aba.set('TODOS');
    this.buscaDigitada.set('');
    this.busca.set('');
    this.categoriaId.set('');
    this.natureza.set('');
    this.situacao.set('');
    this.reiniciar();
  }

  ordenarPor(campo: CampoOrdenacao): void {
    const atual = this.ordenacao();
    if (atual.campo === campo) {
      this.ordenacao.set({ campo, direcao: atual.direcao === 'asc' ? 'desc' : 'asc' });
    } else {
      this.ordenacao.set({ campo, direcao: campo === 'descricao' ? 'asc' : 'desc' });
    }
    this.reiniciar();
  }

  irParaPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.carregar(false);
  }

  recarregar(): void {
    this.carregar(false);
  }

  abrirNovo(): void {
    this.formularioLancamento()?.abrirParaCriar();
  }

  editar(lancamento: Lancamento): void {
    this.formularioLancamento()?.abrirParaEditar(lancamento);
  }

  aoSalvar(): void {
    this.carregar(true);
  }

  alternarPagamento(lancamento: Lancamento): void {
    if (this.processando().has(lancamento.id)) {
      return;
    }
    this.marcarProcessando(lancamento.id, true);

    const requisicao = lancamento.paga
      ? this.lancamentoService.desfazerPagamento(lancamento.id)
      : this.lancamentoService.marcarComoPaga(lancamento.id);

    requisicao.pipe(finalize(() => this.marcarProcessando(lancamento.id, false))).subscribe({
      next: () => {
        this.toasts.sucesso(
          lancamento.paga
            ? `Pagamento de ${lancamento.descricao} desfeito`
            : `${lancamento.descricao} marcado como ${lancamento.tipo === 'RECEITA' ? 'recebido' : 'pago'}`,
        );
        this.carregar(true);
      },
      error: (erro: unknown) => this.toasts.erro(descreverErro(erro)),
    });
  }

  pedirExclusao(lancamento: Lancamento): void {
    this.lancamentoParaExcluir.set(lancamento);
    this.dialogoExclusao()?.abrir();
  }

  confirmarExclusao(): void {
    const lancamento = this.lancamentoParaExcluir();
    if (lancamento === null) {
      return;
    }
    this.lancamentoParaExcluir.set(null);
    this.marcarProcessando(lancamento.id, true);

    this.lancamentoService
      .excluir(lancamento.id)
      .pipe(finalize(() => this.marcarProcessando(lancamento.id, false)))
      .subscribe({
        next: () => {
          this.toasts.sucesso(`${lancamento.descricao} excluído`);
          this.carregar(true);
        },
        error: (erro: unknown) => this.toasts.erro(descreverErro(erro)),
      });
  }

  ariaOrdenacao(campo: CampoOrdenacao): 'ascending' | 'descending' | 'none' {
    const atual = this.ordenacao();
    if (atual.campo !== campo) {
      return 'none';
    }
    return atual.direcao === 'asc' ? 'ascending' : 'descending';
  }

  setaDe(campo: CampoOrdenacao): string {
    const atual = this.ordenacao();
    if (atual.campo !== campo) {
      return '';
    }
    return atual.direcao === 'asc' ? '↑' : '↓';
  }

  rotuloSituacao(lancamento: Lancamento): string {
    const receita = lancamento.tipo === 'RECEITA';
    if (lancamento.paga) {
      return receita ? 'Recebido' : 'Pago';
    }
    if (lancamento.vencido) {
      return 'Vencido';
    }
    return receita ? 'A receber' : 'Em aberto';
  }

  classeSituacao(lancamento: Lancamento): string {
    if (lancamento.paga) {
      return 'selo--pago';
    }
    return lancamento.vencido ? 'selo--vencido' : '';
  }

  rotuloAcaoPagamento(lancamento: Lancamento): string {
    if (lancamento.paga) {
      return `Desfazer pagamento de ${lancamento.descricao}`;
    }
    return `Marcar ${lancamento.descricao} como ${lancamento.tipo === 'RECEITA' ? 'recebido' : 'pago'}`;
  }

  sinalDoValor(lancamento: Lancamento): string {
    return lancamento.tipo === 'RECEITA' ? '+' : '−';
  }

  private reiniciar(): void {
    this.pagina.set(0);
    this.carregar(false);
  }

  private marcarProcessando(id: string, ativo: boolean): void {
    this.processando.update((atual) => {
      const copia = new Set(atual);
      if (ativo) {
        copia.add(id);
      } else {
        copia.delete(id);
      }
      return copia;
    });
  }

  private carregar(silencioso: boolean): void {
    this.requisicao?.unsubscribe();
    if (!silencioso) {
      this.carregando.set(true);
    }
    this.erro.set(null);

    this.requisicao = this.lancamentoService
      .listar(this.filtro(), this.pagina(), TAMANHO_PAGINA, this.ordenacao())
      .subscribe({
        next: (resultado) => {
          if (resultado.conteudo.length === 0 && this.pagina() > 0) {
            this.pagina.set(Math.max(resultado.totalPaginas - 1, 0));
            this.carregar(silencioso);
            return;
          }
          this.resultado.set(resultado);
          this.carregando.set(false);
        },
        error: (erro: unknown) => {
          this.carregando.set(false);
          this.erro.set(descreverErro(erro));
        },
      });
  }
}
