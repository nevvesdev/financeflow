import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { mesAtual } from '../mes';
import { SeletorMes } from './seletor-mes';

function criar(mes: string) {
  const fixture = TestBed.createComponent(SeletorMes);
  fixture.componentRef.setInput('mes', mes);
  const emitidos: string[] = [];
  fixture.componentInstance.mesAlterado.subscribe((valor) => emitidos.push(valor));
  return { componente: fixture.componentInstance, emitidos };
}

describe('SeletorMes', () => {
  it('deve descrever o mês por extenso', () => {
    const { componente } = criar('2026-11');

    expect(componente.rotulo()).toBe('Novembro de 2026');
  });

  it('deve emitir o mês anterior e o próximo', () => {
    const { componente, emitidos } = criar('2026-12');

    componente.anterior();
    componente.proximo();

    expect(emitidos).toEqual(['2026-11', '2027-01']);
  });

  it('deve voltar para o mês atual', () => {
    const { componente, emitidos } = criar('2020-01');

    componente.irParaHoje();

    expect(emitidos).toEqual([mesAtual()]);
  });

  it('deve saber se está no mês atual', () => {
    expect(criar(mesAtual()).componente.ehMesAtual()).toBe(true);
    expect(criar('2020-01').componente.ehMesAtual()).toBe(false);
  });
});
