import { Pipe, PipeTransform } from '@angular/core';

import { formatarMoeda } from '../formatadores';

@Pipe({ name: 'moeda' })
export class MoedaPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    return formatarMoeda(valor ?? 0);
  }
}
