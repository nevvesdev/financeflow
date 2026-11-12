import { describe, expect, it } from 'vitest';

import { iniciaisDe } from './texto';

describe('iniciaisDe', () => {
  it('deve usar a primeira e a última inicial', () => {
    expect(iniciaisDe('Maria da Silva Souza')).toBe('MS');
  });

  it('deve usar uma só inicial para nomes simples', () => {
    expect(iniciaisDe('joão')).toBe('J');
  });

  it('deve devolver interrogação quando não há nome', () => {
    expect(iniciaisDe(null)).toBe('?');
    expect(iniciaisDe('   ')).toBe('?');
  });
});
