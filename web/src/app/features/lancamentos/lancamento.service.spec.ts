import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { LancamentoService } from './lancamento.service';

describe('LancamentoService', () => {
  it('deve marcar um lançamento como pago', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const servico = TestBed.inject(LancamentoService);
    const controle = TestBed.inject(HttpTestingController);

    servico.marcarComoPaga('abc-123').subscribe();

    const requisicao = controle.expectOne('/api/lancamentos/abc-123/pagamento');
    expect(requisicao.request.method).toBe('POST');
    controle.verify();
  });
});
