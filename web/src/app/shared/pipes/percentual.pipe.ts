import { Pipe, PipeTransform } from '@angular/core';

import { formatarPercentual } from '../formatadores';

@Pipe({ name: 'percentual' })
export class PercentualPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    return formatarPercentual(valor ?? 0);
  }
}
