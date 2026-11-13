import { describe, expect, it } from 'vitest';

import { calcularPeriodo, rotuloDoPeriodo } from './periodo';

const HOJE = new Date(2026, 10, 12);

describe('periodo', () => {
  it('deve calcular este mês', () => {
    expect(calcularPeriodo('este-mes', HOJE)).toEqual({ inicio: '2026-11-01', fim: '2026-11-30' });
  });

  it('deve calcular o mês passado', () => {
    expect(calcularPeriodo('mes-passado', HOJE)).toEqual({ inicio: '2026-10-01', fim: '2026-10-31' });
  });

  it('deve calcular o mês passado virando o ano', () => {
    expect(calcularPeriodo('mes-passado', new Date(2027, 0, 15))).toEqual({
      inicio: '2026-12-01',
      fim: '2026-12-31',
    });
  });

  it('deve calcular os últimos 30 dias incluindo hoje', () => {
    expect(calcularPeriodo('ultimos-30-dias', HOJE)).toEqual({
      inicio: '2026-10-14',
      fim: '2026-11-12',
    });
  });

  it('deve calcular este ano e todo o período', () => {
    expect(calcularPeriodo('este-ano', HOJE)).toEqual({ inicio: '2026-01-01', fim: '2026-12-31' });
    expect(calcularPeriodo('todo-periodo', HOJE)).toEqual({ inicio: null, fim: null });
  });

  it('deve nomear períodos que coincidem com um atalho', () => {
    expect(rotuloDoPeriodo({ inicio: '2026-11-01', fim: '2026-11-30' }, HOJE)).toBe('Este mês');
    expect(rotuloDoPeriodo({ inicio: null, fim: null }, HOJE)).toBe('Todo o período');
  });

  it('deve descrever períodos personalizados', () => {
    expect(rotuloDoPeriodo({ inicio: '2026-11-05', fim: '2026-11-20' }, HOJE)).toBe(
      '05/11/2026 – 20/11/2026',
    );
    expect(rotuloDoPeriodo({ inicio: '2026-11-05', fim: null }, HOJE)).toBe('A partir de 05/11/2026');
    expect(rotuloDoPeriodo({ inicio: null, fim: '2026-11-20' }, HOJE)).toBe('Até 20/11/2026');
  });
});