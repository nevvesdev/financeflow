import { describe, expect, it } from 'vitest';

import { proximoIndice, valorDoEvento } from './eventos';

describe('eventos', () => {
  it('deve ler o valor do elemento que originou o evento', () => {
    const campo = document.createElement('input');
    campo.value = 'mercado';
    let capturado = '';
    campo.addEventListener('input', (evento) => {
      capturado = valorDoEvento(evento);
    });

    campo.dispatchEvent(new Event('input'));

    expect(capturado).toBe('mercado');
  });

  it('deve avançar e voltar com volta circular', () => {
    expect(proximoIndice(0, 3, 'ArrowDown')).toBe(1);
    expect(proximoIndice(2, 3, 'ArrowRight')).toBe(0);
    expect(proximoIndice(0, 3, 'ArrowUp')).toBe(2);
    expect(proximoIndice(1, 3, 'ArrowLeft')).toBe(0);
  });

  it('deve ir ao início e ao fim', () => {
    expect(proximoIndice(1, 4, 'Home')).toBe(0);
    expect(proximoIndice(1, 4, 'End')).toBe(3);
  });

  it('deve ignorar outras teclas', () => {
    expect(proximoIndice(1, 4, 'a')).toBeNull();
  });
});