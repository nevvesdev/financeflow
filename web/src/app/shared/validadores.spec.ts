import { FormControl, FormGroup } from '@angular/forms';
import { describe, expect, it } from 'vitest';

import { camposIguais } from './validadores';

describe('camposIguais', () => {
  const grupo = (senha: string, confirmacao: string) =>
    new FormGroup(
      { senha: new FormControl(senha), confirmacao: new FormControl(confirmacao) },
      { validators: camposIguais('senha', 'confirmacao') },
    );

  it('deve aceitar valores iguais', () => {
    expect(grupo('abc12345', 'abc12345').valid).toBe(true);
  });

  it('deve rejeitar valores diferentes', () => {
    expect(grupo('abc12345', 'outra').hasError('camposDiferentes')).toBe(true);
  });
});
