import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function lerValor(texto: string): number | null {
  const limpo = texto.replace(/R\$|\s/g, '');
  if (limpo === '') {
    return null;
  }

  let normalizado: string;
  if (limpo.includes(',')) {
    normalizado = limpo.replace(/\./g, '').replace(',', '.');
  } else if (/^\d{1,3}(\.\d{3})+$/.test(limpo)) {
    normalizado = limpo.replace(/\./g, '');
  } else {
    normalizado = limpo;
  }

  if (!/^\d+(\.\d{1,2})?$/.test(normalizado)) {
    return null;
  }
  const valor = Number(normalizado);
  return valor > 0 ? valor : null;
}

export function formatarValorParaCampo(valor: number): string {
  return valor.toFixed(2).replace('.', ',');
}

export const valorMonetarioValido: ValidatorFn = (
  controle: AbstractControl,
): ValidationErrors | null => {
  const texto = String(controle.value ?? '');
  return texto.trim() === '' || lerValor(texto) !== null ? null : { valorInvalido: true };
};