import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { CategoriaService } from './categoria.service';

function iniciar() {
  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting()],
  });
  return {
    servico: TestBed.inject(CategoriaService),
    controle: TestBed.inject(HttpTestingController),
  };
}

describe('CategoriaService', () => {
  it('deve listar todas as categorias', () => {
    const { servico, controle } = iniciar();

    servico.listar().subscribe();

    const requisicao = controle.expectOne('/api/categorias');
    expect(requisicao.request.params.has('tipo')).toBe(false);
    controle.verify();
  });

  it('deve filtrar por tipo', () => {
    const { servico, controle } = iniciar();

    servico.listar('DESPESA').subscribe();

    controle.expectOne((req) => req.url === '/api/categorias' && req.params.get('tipo') === 'DESPESA');
    controle.verify();
  });
});