import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { AutenticacaoService } from '../../core/autenticacao/autenticacao.service';
import { ToastService } from '../../core/toast/toast.service';
import { mesAtual } from '../../shared/mes';
import { LancamentoService } from '../lancamentos/lancamento.service';
import { ContaAVencer, Painel } from './painel.modelos';
import { PainelService } from './painel.service';
import { PaginaPainel } from './pagina-painel';

const conta: ContaAVencer = {
  id: 'c-1',
  descricao: 'Aluguel',
  valor: 1500,
  categoriaNome: 'Moradia',
  natureza: 'FIXA',
  dataVencimento: '2026-11-05',
  diasParaVencer: -2,
  vencido: true,
};

function painelExemplo(): Painel {
  return {
    mes: '2026-11',
    inicio: '2026-11-01',
    fim: '2026-11-30',
    previsto: { entradas: 5000, saidas: 2000, saldo: 3000 },
    realizado: { entradas: 5000, saidas: 500, saldo: 4500 },
    contasAVencer: { ate: '2026-11-12', quantidade: 1, valorTotal: 1500, itens: [conta] },
    gastosPorCategoria: [
      { categoriaId: 'k-1', categoriaNome: 'Moradia', total: 1500, percentual: 75 },
      { categoriaId: 'k-2', categoriaNome: 'Mercado', total: 500, percentual: 25 },
    ],
    despesasPorNatureza: { fixas: 1500, variaveis: 500 },
  };
}

describe('PaginaPainel', () => {
  const painelService = { buscar: vi.fn() };
  const lancamentoService = { marcarComoPaga: vi.fn() };
  const toasts = { sucesso: vi.fn(), erro: vi.fn() };
  const autenticacao = { usuario: () => ({ id: 'u-1', nome: 'Maria Silva', email: 'maria@x.dev' }) };

  beforeEach(() => {
    painelService.buscar.mockReset();
    painelService.buscar.mockReturnValue(of(painelExemplo()));
    lancamentoService.marcarComoPaga.mockReset();
    lancamentoService.marcarComoPaga.mockReturnValue(of(null));
    toasts.sucesso.mockReset();
    toasts.erro.mockReset();
    TestBed.configureTestingModule({
      providers: [
        { provide: PainelService, useValue: painelService },
        { provide: LancamentoService, useValue: lancamentoService },
        { provide: ToastService, useValue: toasts },
        { provide: AutenticacaoService, useValue: autenticacao },
      ],
    });
  });

  it('deve carregar o painel do mês atual ao abrir', () => {
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;

    expect(painelService.buscar).toHaveBeenCalledWith(mesAtual());
    expect(componente.painel()?.previsto.saldo).toBe(3000);
    expect(componente.carregando()).toBe(false);
    expect(componente.primeiroNome()).toBe('Maria');
  });

  it('deve buscar de novo ao trocar de mês', () => {
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;

    componente.alterarMes('2026-10');

    expect(componente.mes()).toBe('2026-10');
    expect(painelService.buscar).toHaveBeenLastCalledWith('2026-10');
  });

  it('deve mostrar o erro quando a carga falha e permitir tentar de novo', () => {
    painelService.buscar.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 500, error: { detail: 'Falha no servidor' } })),
    );
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;

    expect(componente.erro()).toBe('Falha no servidor');
    expect(componente.painel()).toBeNull();

    painelService.buscar.mockReturnValue(of(painelExemplo()));
    componente.recarregar();

    expect(componente.erro()).toBeNull();
    expect(componente.painel()).not.toBeNull();
  });

  it('deve marcar como paga, avisar e recarregar sem esconder o conteúdo', () => {
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;

    componente.marcarComoPaga(conta);

    expect(lancamentoService.marcarComoPaga).toHaveBeenCalledWith('c-1');
    expect(toasts.sucesso).toHaveBeenCalledWith('Aluguel marcada como paga');
    expect(painelService.buscar).toHaveBeenCalledTimes(2);
    expect(componente.carregando()).toBe(false);
    expect(componente.painel()).not.toBeNull();
    expect(componente.pagando().size).toBe(0);
  });

  it('deve avisar quando marcar como paga falha e não recarregar', () => {
    lancamentoService.marcarComoPaga.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 400, error: { detail: 'lançamento já está pago' } })),
    );
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;

    componente.marcarComoPaga(conta);

    expect(toasts.erro).toHaveBeenCalledWith('lançamento já está pago');
    expect(painelService.buscar).toHaveBeenCalledTimes(1);
    expect(componente.pagando().size).toBe(0);
  });

  it('não deve marcar a mesma conta duas vezes enquanto a primeira está pendente', () => {
    const pendente = new Subject<unknown>();
    lancamentoService.marcarComoPaga.mockReturnValue(pendente);
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;

    componente.marcarComoPaga(conta);
    componente.marcarComoPaga(conta);

    expect(lancamentoService.marcarComoPaga).toHaveBeenCalledTimes(1);
    expect(componente.pagando().has('c-1')).toBe(true);

    pendente.next(null);
    pendente.complete();

    expect(componente.pagando().has('c-1')).toBe(false);
  });

  it('deve avisar com toast quando a recarga silenciosa falha, mantendo o conteúdo', () => {
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;
    painelService.buscar.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 500, error: { detail: 'Falha' } })),
    );

    componente.marcarComoPaga(conta);

    expect(toasts.erro).toHaveBeenCalledWith('Falha');
    expect(componente.erro()).toBeNull();
    expect(componente.painel()).not.toBeNull();
  });

  it('deve calcular a divisão entre despesas fixas e variáveis', () => {
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;

    expect(componente.totalDespesasPorNatureza()).toBe(2000);
    expect(componente.percentualFixas()).toBe(75);
    expect(componente.percentualVariaveis()).toBe(25);
  });

  it('deve limitar o percentual realizado a 100 e tratar previsto zerado', () => {
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;

    expect(componente.percentualRealizado(500, 2000)).toBe(25);
    expect(componente.percentualRealizado(3000, 2000)).toBe(100);
    expect(componente.percentualRealizado(100, 0)).toBe(0);
  });

  it('deve descrever o prazo da conta', () => {
    const componente = TestBed.createComponent(PaginaPainel).componentInstance;

    expect(componente.prazo(conta)).toBe('Venceu há 2 dias');
  });
});
