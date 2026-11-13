import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { ToastService } from '../../core/toast/toast.service';
import { CategoriaService } from '../categorias/categoria.service';
import { Lancamento, Pagina } from './lancamento.modelos';
import { LancamentoService } from './lancamento.service';
import { PaginaLancamentos } from './pagina-lancamentos';

function lancamento(sobrescrita: Partial<Lancamento> = {}): Lancamento {
  return {
    id: 'l-1',
    descricao: 'Aluguel',
    valor: 1500,
    tipo: 'DESPESA',
    natureza: 'FIXA',
    categoriaId: 'k-1',
    categoriaNome: 'Moradia',
    dataLancamento: '2026-11-05',
    dataVencimento: '2026-11-05',
    paga: false,
    dataPagamento: null,
    vencido: false,
    criadoEm: '2026-11-01T10:00:00Z',
    atualizadoEm: '2026-11-01T10:00:00Z',
    ...sobrescrita,
  };
}

function pagina(conteudo: Lancamento[], sobrescrita: Partial<Pagina<Lancamento>> = {}): Pagina<Lancamento> {
  return {
    conteudo,
    pagina: 0,
    tamanho: 15,
    totalElementos: conteudo.length,
    totalPaginas: 1,
    ...sobrescrita,
  };
}

const CATEGORIAS = [
  { id: 'k-1', nome: 'Moradia', tipo: 'DESPESA' as const },
  { id: 'k-2', nome: 'Salário', tipo: 'RECEITA' as const },
];

describe('PaginaLancamentos', () => {
  const lancamentoService = {
    listar: vi.fn(),
    marcarComoPaga: vi.fn(),
    desfazerPagamento: vi.fn(),
    excluir: vi.fn(),
  };
  const categoriaService = { listar: vi.fn() };
  const toasts = { sucesso: vi.fn(), erro: vi.fn() };

  function criar() {
    return TestBed.createComponent(PaginaLancamentos).componentInstance;
  }

  function ultimaChamada() {
    return lancamentoService.listar.mock.calls.at(-1) as unknown[];
  }

  beforeEach(() => {
    vi.useFakeTimers();
    lancamentoService.listar.mockReset().mockReturnValue(of(pagina([lancamento()])));
    lancamentoService.marcarComoPaga.mockReset().mockReturnValue(of(lancamento()));
    lancamentoService.desfazerPagamento.mockReset().mockReturnValue(of(lancamento()));
    lancamentoService.excluir.mockReset().mockReturnValue(of(undefined));
    categoriaService.listar.mockReset().mockReturnValue(of(CATEGORIAS));
    toasts.sucesso.mockReset();
    toasts.erro.mockReset();
    TestBed.configureTestingModule({
      providers: [
        { provide: LancamentoService, useValue: lancamentoService },
        { provide: CategoriaService, useValue: categoriaService },
        { provide: ToastService, useValue: toasts },
      ],
    });
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('deve carregar a primeira página do mês atual e as categorias', () => {
    const componente = criar();

    expect(ultimaChamada()[0]).toMatchObject({ inicio: expect.any(String), fim: expect.any(String) });
    expect(ultimaChamada().slice(1)).toEqual([0, 15, { campo: 'dataLancamento', direcao: 'desc' }]);
    expect(componente.resultado()?.conteudo).toHaveLength(1);
    expect(componente.categorias()).toEqual(CATEGORIAS);
    expect(componente.carregando()).toBe(false);
  });

  it('deve filtrar por tipo ao trocar de aba e voltar para a primeira página', () => {
    const componente = criar();
    componente.irParaPagina(2);

    componente.selecionarAba('DESPESA');

    expect(ultimaChamada()[0]).toMatchObject({ tipo: 'DESPESA' });
    expect(ultimaChamada()[1]).toBe(0);
  });

  it('deve limpar a categoria que não pertence ao tipo da nova aba', () => {
    const componente = criar();
    componente.categoriaId.set('k-1');

    componente.selecionarAba('RECEITA');

    expect(componente.categoriaId()).toBe('');
    expect(componente.categoriasDoFiltro().map((categoria) => categoria.id)).toEqual(['k-2']);
  });

  it('deve ignorar abas desconhecidas', () => {
    const componente = criar();
    const chamadas = lancamentoService.listar.mock.calls.length;

    componente.selecionarAba('OUTRA');

    expect(lancamentoService.listar.mock.calls.length).toBe(chamadas);
  });

  it('deve traduzir a situação escolhida para os filtros da api', () => {
    const componente = criar();

    componente.situacao.set('PAGOS');
    expect(componente.filtro()).toMatchObject({ paga: true, vencidos: undefined });

    componente.situacao.set('ABERTOS');
    expect(componente.filtro()).toMatchObject({ paga: false, vencidos: undefined });

    componente.situacao.set('VENCIDOS');
    expect(componente.filtro()).toMatchObject({ paga: undefined, vencidos: true });
  });

  it('deve esperar o usuário parar de digitar antes de buscar', () => {
    const componente = criar();
    const chamadas = lancamentoService.listar.mock.calls.length;
    const campo = document.createElement('input');
    campo.addEventListener('input', (evento) => componente.aoDigitar(evento));

    campo.value = 'mer';
    campo.dispatchEvent(new Event('input'));
    campo.value = 'mercado';
    campo.dispatchEvent(new Event('input'));

    expect(lancamentoService.listar.mock.calls.length).toBe(chamadas);

    vi.advanceTimersByTime(300);

    expect(lancamentoService.listar.mock.calls.length).toBe(chamadas + 1);
    expect(ultimaChamada()[0]).toMatchObject({ busca: 'mercado' });
  });

  it('deve alternar a direção ao ordenar pela mesma coluna e começar em ordem própria nas outras', () => {
    const componente = criar();

    componente.ordenarPor('dataLancamento');
    expect(componente.ordenacao()).toEqual({ campo: 'dataLancamento', direcao: 'asc' });

    componente.ordenarPor('descricao');
    expect(componente.ordenacao()).toEqual({ campo: 'descricao', direcao: 'asc' });

    componente.ordenarPor('valor');
    expect(componente.ordenacao()).toEqual({ campo: 'valor', direcao: 'desc' });
    expect(componente.ariaOrdenacao('valor')).toBe('descending');
    expect(componente.ariaOrdenacao('descricao')).toBe('none');
    expect(componente.setaDe('valor')).toBe('↓');
  });

  it('deve carregar a página escolhida na paginação', () => {
    const componente = criar();

    componente.irParaPagina(3);

    expect(ultimaChamada()[1]).toBe(3);
    expect(componente.pagina()).toBe(3);
  });

  it('deve voltar para a última página existente quando a atual fica vazia', () => {
    const componente = criar();
    lancamentoService.listar
      .mockReturnValueOnce(of(pagina([], { pagina: 2, totalPaginas: 2 })))
      .mockReturnValue(of(pagina([lancamento()], { pagina: 1, totalPaginas: 2 })));

    componente.irParaPagina(2);

    expect(componente.pagina()).toBe(1);
    expect(componente.resultado()?.conteudo).toHaveLength(1);
  });

  it('deve mostrar o erro e permitir tentar novamente', () => {
    lancamentoService.listar.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 500, error: { detail: 'Falha no servidor' } })),
    );
    const componente = criar();

    expect(componente.erro()).toBe('Falha no servidor');

    lancamentoService.listar.mockReturnValue(of(pagina([lancamento()])));
    componente.recarregar();

    expect(componente.erro()).toBeNull();
    expect(componente.resultado()).not.toBeNull();
  });

  it('deve limpar todos os filtros e voltar para a primeira página', () => {
    const componente = criar();
    componente.aba.set('DESPESA');
    componente.categoriaId.set('k-1');
    componente.natureza.set('FIXA');
    componente.situacao.set('PAGOS');
    componente.busca.set('mercado');
    expect(componente.temFiltros()).toBe(true);

    componente.limparFiltros();

    expect(componente.temFiltros()).toBe(false);
    expect(componente.buscaDigitada()).toBe('');
    expect(ultimaChamada()[0]).toMatchObject({ tipo: undefined, natureza: undefined, paga: undefined });
  });

  it('deve marcar como pago, avisar e recarregar sem esconder a tabela', () => {
    const componente = criar();

    componente.alternarPagamento(lancamento());

    expect(lancamentoService.marcarComoPaga).toHaveBeenCalledWith('l-1');
    expect(toasts.sucesso).toHaveBeenCalledWith('Aluguel marcado como pago');
    expect(componente.carregando()).toBe(false);
    expect(componente.resultado()).not.toBeNull();
    expect(componente.processando().size).toBe(0);
  });

  it('deve dizer recebido para receitas e desfazer o pagamento de lançamentos pagos', () => {
    const componente = criar();

    componente.alternarPagamento(lancamento({ id: 'l-2', descricao: 'Salário', tipo: 'RECEITA' }));
    expect(toasts.sucesso).toHaveBeenCalledWith('Salário marcado como recebido');

    componente.alternarPagamento(lancamento({ paga: true }));
    expect(lancamentoService.desfazerPagamento).toHaveBeenCalledWith('l-1');
    expect(toasts.sucesso).toHaveBeenCalledWith('Pagamento de Aluguel desfeito');
  });

  it('deve avisar quando a mudança de pagamento falha', () => {
    lancamentoService.marcarComoPaga.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 400, error: { detail: 'lançamento já está pago' } })),
    );
    const componente = criar();

    componente.alternarPagamento(lancamento());

    expect(toasts.erro).toHaveBeenCalledWith('lançamento já está pago');
    expect(componente.processando().size).toBe(0);
  });

  it('deve excluir somente depois da confirmação', () => {
    const componente = criar();

    componente.pedirExclusao(lancamento());

    expect(lancamentoService.excluir).not.toHaveBeenCalled();
    expect(componente.mensagemExclusao()).toContain('Aluguel');

    componente.confirmarExclusao();

    expect(lancamentoService.excluir).toHaveBeenCalledWith('l-1');
    expect(toasts.sucesso).toHaveBeenCalledWith('Aluguel excluído');
    expect(componente.lancamentoParaExcluir()).toBeNull();
  });

  it('não deve excluir sem um pedido de exclusão', () => {
    const componente = criar();

    componente.confirmarExclusao();

    expect(lancamentoService.excluir).not.toHaveBeenCalled();
  });

  it('deve descrever situação, classe e ações por tipo de lançamento', () => {
    const componente = criar();

    expect(componente.rotuloSituacao(lancamento({ paga: true }))).toBe('Pago');
    expect(componente.rotuloSituacao(lancamento({ paga: true, tipo: 'RECEITA' }))).toBe('Recebido');
    expect(componente.rotuloSituacao(lancamento({ vencido: true }))).toBe('Vencido');
    expect(componente.rotuloSituacao(lancamento({ tipo: 'RECEITA' }))).toBe('A receber');
    expect(componente.rotuloSituacao(lancamento())).toBe('Em aberto');
    expect(componente.classeSituacao(lancamento({ paga: true }))).toBe('selo--pago');
    expect(componente.classeSituacao(lancamento({ vencido: true }))).toBe('selo--vencido');
    expect(componente.sinalDoValor(lancamento({ tipo: 'RECEITA' }))).toBe('+');
    expect(componente.sinalDoValor(lancamento())).toBe('−');
    expect(componente.rotuloAcaoPagamento(lancamento())).toBe('Marcar Aluguel como pago');
    expect(componente.rotuloAcaoPagamento(lancamento({ paga: true }))).toBe(
      'Desfazer pagamento de Aluguel',
    );
  });
});