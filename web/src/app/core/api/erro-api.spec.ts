import { HttpErrorResponse } from '@angular/common/http';
import { describe, expect, it } from 'vitest';

import { descreverErro, errosPorCampo } from './erro-api';

describe('erro-api', () => {
  it('deve usar o detalhe do problema devolvido pela api', () => {
    const erro = new HttpErrorResponse({ status: 401, error: { detail: 'Credenciais inválidas' } });

    expect(descreverErro(erro)).toBe('Credenciais inválidas');
  });

  it('deve avisar quando não há conexão com o servidor', () => {
    const erro = new HttpErrorResponse({ status: 0, error: new ProgressEvent('error') });

    expect(descreverErro(erro)).toContain('Não foi possível conectar');
  });

  it('deve usar a mensagem padrão para erros desconhecidos', () => {
    expect(descreverErro(new Error('qualquer coisa'))).toBe(
      'Algo deu errado. Tente novamente em instantes.',
    );
  });

  it('deve extrair os erros por campo', () => {
    const erro = new HttpErrorResponse({
      status: 400,
      error: { erros: { email: 'deve ser um endereço de e-mail válido' } },
    });

    expect(errosPorCampo(erro)).toEqual({ email: 'deve ser um endereço de e-mail válido' });
  });

  it('deve devolver um mapa vazio quando não há erros por campo', () => {
    expect(errosPorCampo(new Error('x'))).toEqual({});
  });
});
