import { describe, expect, it } from 'vitest';

import { mesAtual, nomeDoMes, somarMeses } from './mes';

describe('mes', () => {
  it('deve somar meses dentro do mesmo ano', () => {
    expect(somarMeses('2026-05', 1)).toBe('2026-06');
    expect(somarMeses('2026-05', -1)).toBe('2026-04');
  });

  it('deve virar o ano ao avançar e ao voltar', () => {
    expect(somarMeses('2026-12', 1)).toBe('2027-01');
    expect(somarMeses('2026-01', -1)).toBe('2025-12');
    expect(somarMeses('2026-11', 14)).toBe('2028-01');
  });

  it('deve descrever o mês por extenso', () => {
    expect(nomeDoMes('2026-11')).toBe('Novembro de 2026');
    expect(nomeDoMes('2027-03')).toBe('Março de 2027');
  });

  it('deve devolver o mês atual no formato aaaa-mm', () => {
    expect(mesAtual()).toMatch(/^\d{4}-(0[1-9]|1[0-2])$/);
  });
});
