import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function camposIguais(campo: string, confirmacao: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const valor = grupo.get(campo)?.value;
    const copia = grupo.get(confirmacao)?.value;
    return valor === copia ? null : { camposDiferentes: true };
  };
}
