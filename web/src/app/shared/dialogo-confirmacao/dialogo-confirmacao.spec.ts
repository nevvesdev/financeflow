import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { DialogoConfirmacao } from './dialogo-confirmacao';

function criar() {
  const fixture = TestBed.createComponent(DialogoConfirmacao);
  fixture.componentRef.setInput('titulo', 'Excluir lançamento');
  fixture.componentRef.setInput('mensagem', 'Esta ação não pode ser desfeita.');
  fixture.detectChanges();
  const confirmacoes: number[] = [];
  fixture.componentInstance.confirmado.subscribe(() => confirmacoes.push(1));
  const dialogo = fixture.nativeElement.querySelector('dialog') as HTMLDialogElement;
  return { componente: fixture.componentInstance, dialogo, confirmacoes };
}

describe('DialogoConfirmacao', () => {
  it('deve abrir e fechar o diálogo', () => {
    const { componente, dialogo } = criar();

    componente.abrir();
    expect(dialogo.open).toBe(true);

    componente.fechar();
    expect(dialogo.open).toBe(false);
  });

  it('deve emitir a confirmação e fechar', () => {
    const { componente, dialogo, confirmacoes } = criar();
    componente.abrir();

    componente.confirmar();

    expect(confirmacoes).toHaveLength(1);
    expect(dialogo.open).toBe(false);
  });

  it('deve fechar ao clicar no fundo, sem confirmar', () => {
    const { componente, dialogo, confirmacoes } = criar();
    componente.abrir();

    dialogo.dispatchEvent(new MouseEvent('click', { bubbles: true }));

    expect(dialogo.open).toBe(false);
    expect(confirmacoes).toHaveLength(0);
  });

  it('deve usar identificadores de título únicos por instância', () => {
    expect(criar().componente.idTitulo).not.toBe(criar().componente.idTitulo);
  });
});