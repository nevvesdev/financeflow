import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { PainelService } from './painel.service';

function iniciar() {
  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting()],
  });
  return {
    servico: TestBed.inject(PainelService),
    controle: TestBed.inject(HttpTestingController),
  };
}

describe('PainelService', () => {
  it('deve enviar o mês escolhido', () => {
    const { servico, controle } = iniciar();

    servico.buscar('2026-11').subscribe();

    const requisicao = controle.expectOne(
      (req) => req.url === '/api/painel' && req.params.get('mes') === '2026-11',
    );
    expect(requisicao.request.method).toBe('GET');
    controle.verify();
  });

  it('não deve enviar o parâmetro quando o mês não é informado', () => {
    const { servico, controle } = iniciar();

    servico.buscar().subscribe();

    const requisicao = controle.expectOne('/api/painel');
    expect(requisicao.request.params.has('mes')).toBe(false);
    controle.verify();
  });
});
