import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { filter, map } from 'rxjs';

import { AutenticacaoService } from '../core/autenticacao/autenticacao.service';
import { AlternadorTema } from '../shared/alternador-tema/alternador-tema';
import { Marca } from '../shared/marca/marca';
import { iniciaisDe } from '../shared/texto';

interface ItemMenu {
  rotulo: string;
  rota: string;
  icone: string;
}

@Component({
  selector: 'app-layout-principal',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Marca, AlternadorTema],
  templateUrl: './layout-principal.html',
  styleUrl: './layout-principal.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayoutPrincipal {
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly roteador = inject(Router);

  readonly itens: readonly ItemMenu[] = [
    {
      rotulo: 'Painel',
      rota: '/painel',
      icone: 'M3 3h7v9H3z M14 3h7v5h-7z M14 12h7v9h-7z M3 16h7v5H3z',
    },
  ];

  readonly usuario = this.autenticacao.usuario;
  readonly iniciais = computed(() => iniciaisDe(this.usuario()?.nome));
  readonly recolhido = signal(false);

  private readonly urlAtual = toSignal(
    this.roteador.events.pipe(
      filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd),
      map((evento) => evento.urlAfterRedirects),
    ),
    { initialValue: this.roteador.url },
  );

  readonly indiceAtivo = computed(() =>
    this.itens.findIndex((item) => this.urlAtual().startsWith(item.rota)),
  );

  constructor() {
    if (this.autenticacao.usuario() === null) {
      this.autenticacao.carregarUsuario().subscribe({ error: () => undefined });
    }
  }

  alternarMenu(): void {
    this.recolhido.update((valor) => !valor);
  }

  sair(): void {
    this.autenticacao.sair();
  }
}
