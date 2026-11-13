import { Pipe, PipeTransform } from '@angular/core';

import { formatarDataCurta } from '../formatadores';

@Pipe({ name: 'dataCurta' })
export class DataCurtaPipe implements PipeTransform {
  transform(iso: string | null | undefined): string {
    return iso ? formatarDataCurta(iso) : '';
  }
}
