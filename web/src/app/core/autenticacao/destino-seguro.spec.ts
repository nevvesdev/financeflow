import { describe, expect, it } from 'vitest';

import { destinoSeguro } from './destino-seguro';

describe('destinoSeguro', () => {
  it('deve aceitar caminhos internos', () => {
    expect(destinoSeguro('/lancamentos?pagina=2')).toBe('/lancamentos?pagina=2');
  });

  it.each([
    undefined,
    '',
    'https://malicioso.com',
    '//malicioso.com',
    '/\\malicioso.com',
    '/login',
    '/cadastro',
  ])('deve usar o padrão para %s', (retorno) => {
    expect(destinoSeguro(retorno)).toBe('/painel');
  });
});
