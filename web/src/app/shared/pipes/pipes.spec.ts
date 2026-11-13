import { describe, expect, it } from 'vitest';

import { DataCurtaPipe } from './data-curta.pipe';
import { MoedaPipe } from './moeda.pipe';
import { PercentualPipe } from './percentual.pipe';

describe('pipes de formatação', () => {
  it('moeda deve formatar e tratar ausência de valor como zero', () => {
    const pipe = new MoedaPipe();

    expect(pipe.transform(1500)).toContain('1.500,00');
    expect(pipe.transform(null)).toContain('0,00');
  });

  it('percentual deve formatar e tratar ausência de valor como zero', () => {
    const pipe = new PercentualPipe();

    expect(pipe.transform(25)).toBe('25%');
    expect(pipe.transform(undefined)).toBe('0%');
  });

  it('dataCurta deve formatar e tratar ausência de valor como vazio', () => {
    const pipe = new DataCurtaPipe();

    expect(pipe.transform('2026-11-05')).toBe('05 nov');
    expect(pipe.transform(null)).toBe('');
  });
});
