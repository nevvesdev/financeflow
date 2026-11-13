import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { ToastService } from '../../core/toast/toast.service';

@Component({
  selector: 'app-toasts',
  templateUrl: './toasts.html',
  styleUrl: './toasts.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Toasts {
  private readonly servico = inject(ToastService);

  readonly toasts = this.servico.toasts;

  fechar(id: number): void {
    this.servico.fechar(id);
  }
}
