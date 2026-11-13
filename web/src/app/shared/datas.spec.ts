import { describe, expect, it } from 'vitest';

import { hojeIso, paraIso } from './datas';

describe('datas', () => {
  it('deve converter uma data local para o formato aaaa-mm-dd', () => {
    expect(paraIso(new Date(2026, 10, 5))).toBe('2026-11-05');
    expect(paraIso(new Date(2027, 0, 31))).toBe('2027-01-31');
  });

  it('deve devolver hoje no formato aaaa-mm-dd', () => {
    expect(hojeIso()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });
});