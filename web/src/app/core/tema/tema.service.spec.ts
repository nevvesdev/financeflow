import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';

import { CHAVE_TEMA, TemaService } from './tema.service';

describe('TemaService', () => {
  beforeEach(() => {
    localStorage.clear();
    delete document.documentElement.dataset['tema'];
  });

  it('deve começar no tema escuro por padrão', () => {
    const servico = TestBed.inject(TemaService);

    expect(servico.tema()).toBe('escuro');
    expect(document.documentElement.dataset['tema']).toBe('escuro');
  });

  it('deve restaurar o tema claro salvo', () => {
    localStorage.setItem(CHAVE_TEMA, 'claro');

    const servico = TestBed.inject(TemaService);

    expect(servico.tema()).toBe('claro');
    expect(document.documentElement.dataset['tema']).toBe('claro');
  });

  it('deve alternar, aplicar no documento e salvar a escolha', () => {
    const servico = TestBed.inject(TemaService);

    servico.alternar();

    expect(servico.tema()).toBe('claro');
    expect(document.documentElement.dataset['tema']).toBe('claro');
    expect(localStorage.getItem(CHAVE_TEMA)).toBe('claro');

    servico.alternar();

    expect(servico.tema()).toBe('escuro');
    expect(localStorage.getItem(CHAVE_TEMA)).toBe('escuro');
  });

  it('deve ignorar valores desconhecidos no armazenamento', () => {
    localStorage.setItem(CHAVE_TEMA, 'roxo');

    expect(TestBed.inject(TemaService).tema()).toBe('escuro');
  });
});
