import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { Paginacao, janelaDePaginas } from './paginacao';

function criar(pagina: number, totalPaginas: number) {
  const fixture = TestBed.createComponent(Paginacao);
  fixture.componentRef.setInput('pagina', pagina);
  fixture.componentRef.setInput('totalPaginas', totalPaginas);
  const emitidas: number[] = [];
  fixture.componentInstance.paginaAlterada.subscribe((valor) => emitidas.push(valor));
  return { componente: fixture.componentInstance, emitidas };
}

describe('janelaDePaginas', () => {
  it.each([
    [0, 1, [0]],
    [0, 3, [0, 1, 2]],
    [4, 7, [0, 1, 2, 3, 4, 5, 6]],
    [0, 20, [0, 1, 2, 3, 4, null, 19]],
    [3, 20, [0, 1, 2, 3, 4, null, 19]],
    [4, 20, [0, null, 3, 4, 5, null, 19]],
    [10, 20, [0, null, 9, 10, 11, null, 19]],
    [16, 20, [0, null, 15, 16, 17, 18, 19]],
    [19, 20, [0, null, 15, 16, 17, 18, 19]],
  ])('deve montar a janela da página %i de %i', (atual, total, esperado) => {
    expect(janelaDePaginas(atual, total)).toEqual(esperado);
  });
});

describe('Paginacao', () => {
  it('deve localizar a página atual na janela', () => {
    expect(criar(10, 20).componente.indiceAtivo()).toBe(3);
    expect(criar(0, 20).componente.indiceAtivo()).toBe(0);
  });

  it('deve emitir a página escolhida, a anterior e a próxima', () => {
    const { componente, emitidas } = criar(5, 20);

    componente.irPara(9);
    componente.anterior();
    componente.proxima();

    expect(emitidas).toEqual([9, 4, 6]);
  });

  it('não deve emitir para a página atual nem para fora do intervalo', () => {
    const { componente, emitidas } = criar(0, 3);

    componente.irPara(0);
    componente.anterior();
    componente.irPara(3);

    expect(emitidas).toEqual([]);
  });
});