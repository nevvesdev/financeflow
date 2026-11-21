import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { NaturezaLancamento, TipoLancamento } from '../../core/api/dominio';
import { descreverErro, errosPorCampo } from '../../core/api/erro-api';
import { ToastService } from '../../core/toast/toast.service';
import { hojeIso } from '../../shared/datas';
import { formatarValorParaCampo, lerValor, valorMonetarioValido } from '../../shared/valor';
import { Categoria } from '../categorias/categoria.modelos';
import { DadosLancamento, Lancamento } from './lancamento.modelos';
import { LancamentoService } from './lancamento.service';

type Campo = 'descricao' | 'valor' | 'categoriaId' | 'dataLancamento' | 'dataVencimento';

let proximoId = 0;

@Component({
  selector: 'app-formulario-lancamento',
  imports: [ReactiveFormsModule],
  templateUrl: './formulario-lancamento.html',
  styleUrl: './formulario-lancamento.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FormularioLancamento {
  private readonly construtor = inject(NonNullableFormBuilder);
  private readonly lancamentoService = inject(LancamentoService);
  private readonly toasts = inject(ToastService);
  private readonly gaveta = viewChild<ElementRef<HTMLDialogElement>>('gaveta');

  readonly idTitulo = `gaveta-titulo-${proximoId++}`;

  readonly categorias = input.required<readonly Categoria[]>();
  readonly salvo = output<void>();

  readonly lancamentoEmEdicao = signal<Lancamento | null>(null);
  readonly enviando = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly versao = signal(0);

  readonly formulario = this.construtor.group({
    descricao: ['', [Validators.required, Validators.maxLength(160)]],
    valor: ['', [Validators.required, valorMonetarioValido]],
    categoriaId: ['', [Validators.required]],
    natureza: ['VARIAVEL' as NaturezaLancamento],
    dataLancamento: [hojeIso(), [Validators.required]],
    dataVencimento: [''],
    pago: [false],
    dataPagamento: [''],
  });

  readonly editando = computed(() => this.lancamentoEmEdicao() !== null);
  readonly titulo = computed(() => (this.editando() ? 'Editar lançamento' : 'Novo lançamento'));

  readonly categoriasDisponiveis = computed(() => {
    const emEdicao = this.lancamentoEmEdicao();
    const todas = this.categorias();
    return emEdicao ? todas.filter((categoria) => categoria.tipo === emEdicao.tipo) : todas;
  });
  readonly despesas = computed(() =>
    this.categoriasDisponiveis().filter((categoria) => categoria.tipo === 'DESPESA'),
  );
  readonly receitas = computed(() =>
    this.categoriasDisponiveis().filter((categoria) => categoria.tipo === 'RECEITA'),
  );

  abrirParaCriar(): void {
    this.lancamentoEmEdicao.set(null);
    this.reiniciar({
      descricao: '',
      valor: '',
      categoriaId: '',
      natureza: 'VARIAVEL',
      dataLancamento: hojeIso(),
      dataVencimento: '',
      pago: false,
      dataPagamento: '',
    });
    this.mostrar();
  }

  abrirParaEditar(lancamento: Lancamento): void {
    this.lancamentoEmEdicao.set(lancamento);
    this.reiniciar({
      descricao: lancamento.descricao,
      valor: formatarValorParaCampo(lancamento.valor),
      categoriaId: lancamento.categoriaId,
      natureza: lancamento.natureza,
      dataLancamento: lancamento.dataLancamento,
      dataVencimento: lancamento.dataVencimento ?? '',
      pago: false,
      dataPagamento: '',
    });
    this.mostrar();
  }

  fechar(): void {
    const elemento = this.gaveta()?.nativeElement;
    if (!elemento) {
      return;
    }
    if (typeof elemento.close === 'function') {
      elemento.close();
    } else {
      elemento.removeAttribute('open');
    }
  }

  aoClicarNoFundo(evento: MouseEvent): void {
    if (evento.target === this.gaveta()?.nativeElement) {
      this.fechar();
    }
  }

  tipoSelecionado(): TipoLancamento | null {
    const id = this.formulario.controls.categoriaId.value;
    return this.categorias().find((categoria) => categoria.id === id)?.tipo ?? null;
  }

  rotuloPago(): string {
    return this.tipoSelecionado() === 'RECEITA' ? 'Já foi recebido' : 'Já foi pago';
  }

  mensagemDoCampo(campo: Campo): string | null {
    const controle = this.formulario.controls[campo];
    if (!controle.touched || controle.errors === null) {
      return null;
    }
    const erros = controle.errors;
    if (erros['servidor']) {
      return String(erros['servidor']);
    }
    if (erros['required']) {
      return campo === 'categoriaId' ? 'Selecione uma categoria.' : 'Campo obrigatório.';
    }
    if (erros['valorInvalido']) {
      return 'Informe um valor válido, como 1.234,56.';
    }
    if (erros['maxlength']) {
      return 'Texto longo demais para este campo.';
    }
    return null;
  }

  salvar(): void {
    if (this.enviando()) {
      return;
    }
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const valor = lerValor(valores.valor);
    if (valor === null) {
      this.formulario.controls.valor.markAsTouched();
      return;
    }

    const dados: DadosLancamento = {
      descricao: valores.descricao.trim(),
      valor,
      categoriaId: valores.categoriaId,
      dataLancamento: valores.dataLancamento,
      dataVencimento: valores.dataVencimento || null,
      natureza: valores.natureza,
    };

    const emEdicao = this.lancamentoEmEdicao();
    const requisicao = emEdicao
      ? this.lancamentoService.atualizar(emEdicao.id, dados)
      : this.lancamentoService.criar({
          ...dados,
          dataPagamento: valores.pago ? valores.dataPagamento || hojeIso() : null,
        });

    this.enviando.set(true);
    this.erroGeral.set(null);

    requisicao.subscribe({
      next: () => {
        this.enviando.set(false);
        this.toasts.sucesso(emEdicao ? 'Lançamento atualizado' : 'Lançamento criado');
        this.fechar();
        this.salvo.emit();
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.aplicarErros(erro);
      },
    });
  }

  private reiniciar(valores: ReturnType<typeof this.formulario.getRawValue>): void {
    this.formulario.reset(valores);
    this.erroGeral.set(null);
    this.enviando.set(false);
    this.versao.update((atual) => atual + 1);
  }

  private mostrar(): void {
    const elemento = this.gaveta()?.nativeElement;
    if (!elemento || elemento.open) {
      return;
    }
    if (typeof elemento.showModal === 'function') {
      elemento.showModal();
    } else {
      elemento.setAttribute('open', '');
    }
  }

  private aplicarErros(erro: unknown): void {
    let aplicados = 0;
    for (const [campo, mensagem] of Object.entries(errosPorCampo(erro))) {
      const controle = this.formulario.get(campo);
      if (controle) {
        controle.setErrors({ servidor: mensagem });
        controle.markAsTouched();
        aplicados++;
      }
    }
    this.erroGeral.set(aplicados > 0 ? null : descreverErro(erro));
  }
}