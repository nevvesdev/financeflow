import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { ToastService } from './toast.service';

describe('ToastService', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('deve exibir um aviso e removê-lo depois da duração padrão', () => {
    const servico = TestBed.inject(ToastService);

    servico.sucesso('Salvo');

    expect(servico.toasts()).toHaveLength(1);
    expect(servico.toasts()[0]).toMatchObject({ tipo: 'sucesso', mensagem: 'Salvo' });

    vi.advanceTimersByTime(4000);

    expect(servico.toasts()).toHaveLength(0);
  });

  it('deve manter avisos de erro por mais tempo', () => {
    const servico = TestBed.inject(ToastService);

    servico.erro('Falhou');
    vi.advanceTimersByTime(4000);

    expect(servico.toasts()).toHaveLength(1);

    vi.advanceTimersByTime(3000);

    expect(servico.toasts()).toHaveLength(0);
  });

  it('deve fechar um aviso manualmente', () => {
    const servico = TestBed.inject(ToastService);
    servico.info('Olá');

    servico.fechar(servico.toasts()[0].id);

    expect(servico.toasts()).toHaveLength(0);
  });

  it('deve manter no máximo quatro avisos, descartando os mais antigos', () => {
    const servico = TestBed.inject(ToastService);

    ['um', 'dois', 'três', 'quatro', 'cinco'].forEach((mensagem) => servico.info(mensagem));

    expect(servico.toasts().map((toast) => toast.mensagem)).toEqual([
      'dois',
      'três',
      'quatro',
      'cinco',
    ]);
  });
});
