import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LancamentoService {
  private readonly http = inject(HttpClient);

  marcarComoPaga(id: string): Observable<unknown> {
    return this.http.post(`/api/lancamentos/${id}/pagamento`, null);
  }
}
