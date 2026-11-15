import { FormControl } from '@angular/forms';
import { describe, expect, it } from 'vitest';

import { formatarValorParaCampo, lerValor, valorMonetarioValido } from './valor';

describe('lerValor', () => {
  it.each([
    ['25', 25],
    ['25,5', 25.5],
    ['25,50', 25.5],
    ['1.234,56', 1234.56],
    ['R$ 1.234,56', 1234.56],
    ['1234.56', 1234.56],
    ['1.234', 1234],
    ['12.5', 12.5],
    ['0,50', 0.5],
  ])('deve ler %s como %d', (texto, esperado) => {
    expect(lerValor(texto)).toBe(esperado);
  });

  it.each(['', '   ', 'abc', '0', '0,00', '10,505', '-5', '1,2,3', '12,34,56'])(
    'deve rejeitar %j',
    (texto) => {
      expect(lerValor(texto)).toBeNull();
    },
  );
});

describe('formatarValorParaCampo', () => {
  it('deve formatar com vírgula e duas casas', () => {
    expect(formatarValorParaCampo(1500)).toBe('1500,00');
    expect(formatarValorParaCampo(25.5)).toBe('25,50');
  });
});

describe('valorMonetarioValido', () => {
  it('deve aceitar vazio, deixando a obrigatoriedade para outro validador', () => {
    expect(valorMonetarioValido(new FormControl(''))).toBeNull();
  });

  it('deve aceitar valores válidos e rejeitar inválidos', () => {
    expect(valorMonetarioValido(new FormControl('1.234,56'))).toBeNull();
    expect(valorMonetarioValido(new FormControl('abc'))).toEqual({ valorInvalido: true });
  });
});