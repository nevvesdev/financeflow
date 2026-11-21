import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ToastService } from '../../core/toast/toast.service';
import { hojeIso } from '../../shared/datas';
import { Categoria } from '../categorias/categoria.modelos';
import { FormularioLancamento } from './formulario-lancamento';
import { Lancamento } from './lancamento.modelos';
import { LancamentoService } from './lancamento.service';

const CATEGORIAS: Categoria[] = [
  { id: 'k-1', nome: 'Moradia', tipo: 'DESPESA' },
  { id: 'k-2', nome: 'Mercado', tipo: 'DESPESA' },
  { id: 'k-3', nome: 'Salário', tipo: 'RECEITA' },
];

const LANCAMENTO: Lancamento = {
  id: 'l-1',
  descricao: 'Aluguel',
  valor: 1500,
  tipo: 'DESPESA',
  natureza: 'FIXA',
  categoriaId: 'k-1',
  categoriaNome: 'Moradia',
  dataLancamento: '2026-11-05',
  dataVencimento: '2026-11-10',
  paga: false,
  dataPagamento: null,
  vencido: false,
  criadoEm: '2026-11-01T10:00:00Z',
  atualizadoEm: '2026-11-01T10:00:00Z',
};

describe('FormularioLancamento', () => {
  const lancamentoService = { criar: vi.fn(), atualizar: vi.fn() };
  const toasts = { sucesso: vi.fn(), erro: vi.fn() };

  function criar() {
    const fixture = TestBed.createComponent(FormularioLancamento);
    fixture.componentRef.setInput('categorias', CATEGORIAS);
    const salvos: number[] = [];
    fixture.componentInstance.salvo.subscribe(() => salvos.push(1));
    return { fixture, componente: fixture.componentInstance, salvos };
  }

  function preencher(componente: FormularioLancamento, sobrescrita: Record<string, unknown> = {}) {
    componente.formulario.patchValue({
      descricao: 'Mercado do mês',
      valor: '25,5',
      categoriaId: 'k-2',
      ...sobrescrita,
    });
  }

  beforeEach(() => {
    lancamentoService.criar.mockReset().mockReturnValue(of(LANCAMENTO));
    lancamentoService.atualizar.mockReset().mockReturnValue(of(LANCAMENTO));
    toasts.sucesso.mockReset();
    toasts.erro.mockReset();
    TestBed.configureTestingModule({
      providers: [
        { provide: LancamentoService, useValue: lancamentoService },
        { provide: ToastService, useValue: toasts },
      ],
    });
  });

  it('deve abrir para criar com os valores padrão e mostrar a gaveta', () => {
    const { fixture, componente } = criar();
    fixture.detectChanges();

    componente.abrirParaCriar();

    expect(componente.titulo()).toBe('Novo lançamento');
    expect(componente.formulario.getRawValue()).toMatchObject({
      descricao: '',
      valor: '',
      natureza: 'VARIAVEL',
      dataLancamento: hojeIso(),
      pago: false,
    });
    expect((fixture.nativeElement.querySelector('dialog') as HTMLDialogElement).open).toBe(true);
  });

  it('deve abrir para editar com os dados do lançamento e só categorias do mesmo tipo', () => {
    const { componente } = criar();

    componente.abrirParaEditar(LANCAMENTO);

    expect(componente.titulo()).toBe('Editar lançamento');
    expect(componente.formulario.getRawValue()).toMatchObject({
      descricao: 'Aluguel',
      valor: '1500,00',
      categoriaId: 'k-1',
      natureza: 'FIXA',
      dataLancamento: '2026-11-05',
      dataVencimento: '2026-11-10',
    });
    expect(componente.despesas().map((categoria) => categoria.id)).toEqual(['k-1', 'k-2']);
    expect(componente.receitas()).toEqual([]);
  });

  it('deve agrupar as categorias por tipo ao criar e informar o tipo escolhido', () => {
    const { componente } = criar();

    expect(componente.despesas()).toHaveLength(2);
    expect(componente.receitas()).toHaveLength(1);

    componente.formulario.patchValue({ categoriaId: 'k-3' });
    expect(componente.tipoSelecionado()).toBe('RECEITA');
    expect(componente.rotuloPago()).toBe('Já foi recebido');

    componente.formulario.patchValue({ categoriaId: 'k-1' });
    expect(componente.rotuloPago()).toBe('Já foi pago');
  });

  it('não deve enviar com o formulário inválido e deve explicar os campos', () => {
    const { componente } = criar();

    componente.salvar();

    expect(lancamentoService.criar).not.toHaveBeenCalled();
    expect(componente.mensagemDoCampo('descricao')).toBe('Campo obrigatório.');
    expect(componente.mensagemDoCampo('categoriaId')).toBe('Selecione uma categoria.');
  });

  it('deve rejeitar valores que não são dinheiro', () => {
    const { componente } = criar();
    preencher(componente, { valor: 'abc' });

    componente.salvar();

    expect(lancamentoService.criar).not.toHaveBeenCalled();
    expect(componente.mensagemDoCampo('valor')).toBe('Informe um valor válido, como 1.234,56.');
  });

  it('deve criar convertendo o valor, avisar e emitir o salvamento', () => {
    const { componente, salvos } = criar();
    preencher(componente);

    componente.salvar();

    expect(lancamentoService.criar).toHaveBeenCalledWith({
      descricao: 'Mercado do mês',
      valor: 25.5,
      categoriaId: 'k-2',
      dataLancamento: hojeIso(),
      dataVencimento: null,
      natureza: 'VARIAVEL',
      dataPagamento: null,
    });
    expect(toasts.sucesso).toHaveBeenCalledWith('Lançamento criado');
    expect(salvos).toHaveLength(1);
    expect(componente.enviando()).toBe(false);
  });

  it('deve criar já pago usando hoje quando a data de pagamento está vazia', () => {
    const { componente } = criar();
    preencher(componente, { pago: true, dataPagamento: '' });

    componente.salvar();

    expect(lancamentoService.criar).toHaveBeenCalledWith(
      expect.objectContaining({ dataPagamento: hojeIso() }),
    );
  });

  it('deve criar já pago na data informada', () => {
    const { componente } = criar();
    preencher(componente, { pago: true, dataPagamento: '2026-11-03' });

    componente.salvar();

    expect(lancamentoService.criar).toHaveBeenCalledWith(
      expect.objectContaining({ dataPagamento: '2026-11-03' }),
    );
  });

  it('deve atualizar sem enviar dados de pagamento', () => {
    const { componente, salvos } = criar();
    componente.abrirParaEditar(LANCAMENTO);
    componente.formulario.patchValue({ valor: '1.600,00' });

    componente.salvar();

    expect(lancamentoService.atualizar).toHaveBeenCalledWith('l-1', {
      descricao: 'Aluguel',
      valor: 1600,
      categoriaId: 'k-1',
      dataLancamento: '2026-11-05',
      dataVencimento: '2026-11-10',
      natureza: 'FIXA',
    });
    expect(lancamentoService.criar).not.toHaveBeenCalled();
    expect(toasts.sucesso).toHaveBeenCalledWith('Lançamento atualizado');
    expect(salvos).toHaveLength(1);
  });

  it('deve mostrar o erro do servidor no campo correspondente', () => {
    lancamentoService.criar.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { erros: { valor: 'deve ser maior que 0' } },
          }),
      ),
    );
    const { componente, salvos } = criar();
    preencher(componente);

    componente.salvar();

    expect(componente.mensagemDoCampo('valor')).toBe('deve ser maior que 0');
    expect(componente.erroGeral()).toBeNull();
    expect(componente.enviando()).toBe(false);
    expect(salvos).toHaveLength(0);
  });

  it('deve mostrar erros gerais de regra de negócio', () => {
    lancamentoService.criar.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 400, error: { detail: 'categoria não encontrada' } }),
      ),
    );
    const { componente } = criar();
    preencher(componente);

    componente.salvar();

    expect(componente.erroGeral()).toBe('categoria não encontrada');
  });

  it('não deve enviar duas vezes enquanto a primeira requisição está em andamento', () => {
    const pendente = new Subject<Lancamento>();
    lancamentoService.criar.mockReturnValue(pendente);
    const { componente } = criar();
    preencher(componente);

    componente.salvar();
    componente.salvar();

    expect(lancamentoService.criar).toHaveBeenCalledTimes(1);
    expect(componente.enviando()).toBe(true);

    pendente.next(LANCAMENTO);
    pendente.complete();

    expect(componente.enviando()).toBe(false);
  });

  it('deve limpar erros e campos tocados ao reabrir', () => {
    const { componente } = criar();
    componente.salvar();
    expect(componente.mensagemDoCampo('descricao')).not.toBeNull();

    componente.abrirParaCriar();

    expect(componente.mensagemDoCampo('descricao')).toBeNull();
    expect(componente.erroGeral()).toBeNull();
  });
});
