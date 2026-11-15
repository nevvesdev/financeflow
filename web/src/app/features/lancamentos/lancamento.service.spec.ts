import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { LancamentoService } from './lancamento.service';

function iniciar() {
  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting()],
  });
  return {
    servico: TestBed.inject(LancamentoService),
    controle: TestBed.inject(HttpTestingController),
  };
}

const DADOS = {
  descricao: 'Mercado',
  valor: 240.9,
  categoriaId: 'k-1',
  dataLancamento: '2026-11-10',
  dataVencimento: null,
  natureza: 'VARIAVEL' as const,
};

describe('LancamentoService', () => {
  it('deve listar com filtros, paginação e ordenação', () => {
    const { servico, controle } = iniciar();

    servico
      .listar(
        {
          tipo: 'DESPESA',
          busca: 'mercado',
          paga: false,
          inicio: '2026-11-01',
          fim: '2026-11-30',
          categoriaId: undefined,
        },
        2,
        15,
        { campo: 'valor', direcao: 'asc' },
      )
      .subscribe();

    const requisicao = controle.expectOne((req) => req.url === '/api/lancamentos');
    const params = requisicao.request.params;
    expect(requisicao.request.method).toBe('GET');
    expect(params.get('page')).toBe('2');
    expect(params.get('size')).toBe('15');
    expect(params.get('sort')).toBe('valor,asc');
    expect(params.get('tipo')).toBe('DESPESA');
    expect(params.get('busca')).toBe('mercado');
    expect(params.get('paga')).toBe('false');
    expect(params.get('inicio')).toBe('2026-11-01');
    expect(params.get('fim')).toBe('2026-11-30');
    expect(params.has('categoriaId')).toBe(false);
    controle.verify();
  });

  it('deve criar um lançamento', () => {
    const { servico, controle } = iniciar();

    servico.criar({ ...DADOS, dataPagamento: null }).subscribe();

    const requisicao = controle.expectOne('/api/lancamentos');
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual({ ...DADOS, dataPagamento: null });
    controle.verify();
  });

  it('deve atualizar um lançamento', () => {
    const { servico, controle } = iniciar();

    servico.atualizar('abc', DADOS).subscribe();

    const requisicao = controle.expectOne('/api/lancamentos/abc');
    expect(requisicao.request.method).toBe('PUT');
    expect(requisicao.request.body).toEqual(DADOS);
    controle.verify();
  });

  it('deve marcar como pago sem corpo quando não há data', () => {
    const { servico, controle } = iniciar();

    servico.marcarComoPaga('abc').subscribe();

    const requisicao = controle.expectOne('/api/lancamentos/abc/pagamento');
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toBeNull();
    controle.verify();
  });

  it('deve marcar como pago com a data informada', () => {
    const { servico, controle } = iniciar();

    servico.marcarComoPaga('abc', '2026-11-10').subscribe();

    const requisicao = controle.expectOne('/api/lancamentos/abc/pagamento');
    expect(requisicao.request.body).toEqual({ dataPagamento: '2026-11-10' });
    controle.verify();
  });

  it('deve desfazer o pagamento', () => {
    const { servico, controle } = iniciar();

    servico.desfazerPagamento('abc').subscribe();

    const requisicao = controle.expectOne('/api/lancamentos/abc/pagamento');
    expect(requisicao.request.method).toBe('DELETE');
    controle.verify();
  });

  it('deve excluir o lançamento', () => {
    const { servico, controle } = iniciar();

    servico.excluir('abc').subscribe();

    const requisicao = controle.expectOne('/api/lancamentos/abc');
    expect(requisicao.request.method).toBe('DELETE');
    controle.verify();
  });
});