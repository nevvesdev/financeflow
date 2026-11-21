import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ToastService } from '../../core/toast/toast.service';
import { CategoriaService } from '../categorias/categoria.service';
import { Lancamento } from './lancamento.modelos';
import { LancamentoService } from './lancamento.service';
import { PaginaLancamentos } from './pagina-lancamentos';

const LANCAMENTO: Lancamento = {
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
};

describe('PaginaLancamentos com formulário', () => {
  const lancamentoService = { listar: vi.fn() };
  const categoriaService = { listar: vi.fn() };

  beforeEach(() => {
    lancamentoService.listar.mockReset().mockReturnValue(
      of({ conteudo: [LANCAMENTO], pagina: 0, tamanho: 15, totalElementos: 1, totalPaginas: 1 }),
    );
    categoriaService.listar.mockReset().mockReturnValue(of([]));
    TestBed.configureTestingModule({
      providers: [
        { provide: LancamentoService, useValue: lancamentoService },
        { provide: CategoriaService, useValue: categoriaService },
        { provide: ToastService, useValue: { sucesso: vi.fn(), erro: vi.fn() } },
      ],
    });
  });

  it('deve recarregar a lista sem esconder a tabela depois de salvar', () => {
    const componente = TestBed.createComponent(PaginaLancamentos).componentInstance;
    const chamadas = lancamentoService.listar.mock.calls.length;

    componente.aoSalvar();

    expect(lancamentoService.listar.mock.calls.length).toBe(chamadas + 1);
    expect(componente.carregando()).toBe(false);
    expect(componente.resultado()).not.toBeNull();
  });

  it('não deve falhar ao pedir a abertura do formulário antes da renderização', () => {
    const componente = TestBed.createComponent(PaginaLancamentos).componentInstance;

    expect(() => componente.abrirNovo()).not.toThrow();
    expect(() => componente.editar(LANCAMENTO)).not.toThrow();
  });
});
