import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { Periodo, calcularPeriodo } from '../periodo';
import { SeletorPeriodo } from './seletor-periodo';

function criar(periodo: Periodo) {
  const fixture = TestBed.createComponent(SeletorPeriodo);
  fixture.componentRef.setInput('periodo', periodo);
  const emitidos: Periodo[] = [];
  fixture.componentInstance.periodoAlterado.subscribe((valor) => emitidos.push(valor));
  return { componente: fixture.componentInstance, emitidos };
}

describe('SeletorPeriodo', () => {
  it('deve nomear o atalho ativo e o período personalizado', () => {
    expect(criar(calcularPeriodo('este-mes')).componente.rotulo()).toBe('Este mês');
    expect(criar({ inicio: '2026-11-05', fim: '2026-11-20' }).componente.rotulo()).toBe(
      '05/11/2026 – 20/11/2026',
    );
  });

  it('deve marcar o atalho ativo', () => {
    expect(criar(calcularPeriodo('mes-passado')).componente.presetAtivo()).toBe('mes-passado');
    expect(criar({ inicio: '2026-11-05', fim: '2026-11-20' }).componente.presetAtivo()).toBeNull();
  });

  it('deve abrir, preencher o intervalo com o período atual e fechar', () => {
    const { componente } = criar({ inicio: '2026-11-05', fim: '2026-11-20' });

    componente.alternar();

    expect(componente.aberto()).toBe(true);
    expect(componente.inicioPersonalizado()).toBe('2026-11-05');
    expect(componente.fimPersonalizado()).toBe('2026-11-20');

    componente.alternar();

    expect(componente.aberto()).toBe(false);
  });

  it('deve emitir o período do atalho e fechar', () => {
    const { componente, emitidos } = criar(calcularPeriodo('este-mes'));
    componente.alternar();

    componente.escolher('este-ano');

    expect(emitidos).toEqual([calcularPeriodo('este-ano')]);
    expect(componente.aberto()).toBe(false);
  });

  it('deve validar o intervalo personalizado', () => {
    const { componente } = criar(calcularPeriodo('este-mes'));

    componente.inicioPersonalizado.set('2026-11-20');
    componente.fimPersonalizado.set('2026-11-05');
    expect(componente.personalizadoValido()).toBe(false);

    componente.fimPersonalizado.set('2026-11-30');
    expect(componente.personalizadoValido()).toBe(true);

    componente.inicioPersonalizado.set('');
    expect(componente.personalizadoValido()).toBe(false);
  });

  it('só deve aplicar o intervalo personalizado quando válido', () => {
    const { componente, emitidos } = criar(calcularPeriodo('este-mes'));
    componente.inicioPersonalizado.set('2026-11-20');
    componente.fimPersonalizado.set('2026-11-05');

    componente.aplicarPersonalizado();
    expect(emitidos).toEqual([]);

    componente.fimPersonalizado.set('2026-11-25');
    componente.aplicarPersonalizado();
    expect(emitidos).toEqual([{ inicio: '2026-11-20', fim: '2026-11-25' }]);
  });

  it('deve fechar ao pressionar Esc', () => {
    const { componente } = criar(calcularPeriodo('este-mes'));
    componente.alternar();

    componente.aoPressionarEscape();

    expect(componente.aberto()).toBe(false);
  });
});