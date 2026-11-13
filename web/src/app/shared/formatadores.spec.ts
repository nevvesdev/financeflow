import { describe, expect, it } from 'vitest';

import {
  descreverPrazo,
  formatarData,
  formatarDataCurta,
  formatarMoeda,
  formatarPercentual,
} from './formatadores';

describe('formatadores', () => {
  it('deve formatar valores em reais', () => {
    const texto = formatarMoeda(1500);

    expect(texto).toContain('R$');
    expect(texto).toContain('1.500,00');
  });

  it('deve formatar valores negativos', () => {
    expect(formatarMoeda(-25.5)).toContain('25,50');
    expect(formatarMoeda(-25.5)).toContain('-');
  });

  it('não deve mostrar sinal em zero negativo', () => {
    expect(formatarMoeda(-0)).not.toContain('-');
  });

  it('deve formatar percentuais com no máximo uma casa', () => {
    expect(formatarPercentual(75)).toBe('75%');
    expect(formatarPercentual(33.333)).toBe('33,3%');
  });

  it('deve formatar datas sem deslocar o dia por fuso horário', () => {
    expect(formatarData('2026-11-05')).toBe('05/11/2026');
    expect(formatarDataCurta('2026-11-05')).toBe('05 nov');
    expect(formatarDataCurta('2026-01-31')).toBe('31 jan');
  });

  it.each([
    [0, 'Vence hoje'],
    [1, 'Vence amanhã'],
    [5, 'Vence em 5 dias'],
    [-1, 'Venceu ontem'],
    [-3, 'Venceu há 3 dias'],
  ])('deve descrever o prazo de %i dias', (dias, esperado) => {
    expect(descreverPrazo(dias)).toBe(esperado);
  });
});
