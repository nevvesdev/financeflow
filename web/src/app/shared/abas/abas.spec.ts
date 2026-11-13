import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { Abas, ItemAba } from './abas';

const ITENS: readonly ItemAba[] = [
  { valor: 'TODOS', rotulo: 'Todos' },
  { valor: 'RECEITA', rotulo: 'Receitas' },
  { valor: 'DESPESA', rotulo: 'Despesas' },
];

function criar(selecionada: string) {
  const fixture = TestBed.createComponent(Abas);
  fixture.componentRef.setInput('itens', ITENS);
  fixture.componentRef.setInput('selecionada', selecionada);
  fixture.componentRef.setInput('idPainel', 'painel');
  const emitidos: string[] = [];
  fixture.componentInstance.selecionar.subscribe((valor) => emitidos.push(valor));
  return { componente: fixture.componentInstance, emitidos };
}

function tecla(key: string): KeyboardEvent {
  return new KeyboardEvent('keydown', { key, cancelable: true });
}

describe('Abas', () => {
  it('deve calcular o índice da aba selecionada', () => {
    expect(criar('RECEITA').componente.indice()).toBe(1);
    expect(criar('inexistente').componente.indice()).toBe(0);
  });

  it('deve navegar com as setas, com volta circular', () => {
    const { componente, emitidos } = criar('DESPESA');

    componente.aoTeclar(tecla('ArrowRight'));
    componente.aoTeclar(tecla('ArrowLeft'));

    expect(emitidos).toEqual(['TODOS', 'RECEITA']);
  });

  it('deve ir para a primeira e para a última aba', () => {
    const { componente, emitidos } = criar('RECEITA');

    componente.aoTeclar(tecla('Home'));
    componente.aoTeclar(tecla('End'));

    expect(emitidos).toEqual(['TODOS', 'DESPESA']);
  });

  it('deve ignorar teclas não horizontais sem impedir o comportamento padrão', () => {
    const { componente, emitidos } = criar('TODOS');
    const evento = tecla('ArrowDown');

    componente.aoTeclar(evento);

    expect(emitidos).toEqual([]);
    expect(evento.defaultPrevented).toBe(false);
  });
});