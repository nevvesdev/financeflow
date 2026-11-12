import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { descreverErro } from '../../core/api/erro-api';
import { AutenticacaoService } from '../../core/autenticacao/autenticacao.service';
import { destinoSeguro } from '../../core/autenticacao/destino-seguro';
import { Marca } from '../../shared/marca/marca';

@Component({
  selector: 'app-pagina-login',
  imports: [ReactiveFormsModule, RouterLink, Marca],
  templateUrl: './pagina-login.html',
  styleUrl: './autenticacao.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginaLogin {
  private readonly construtor = inject(NonNullableFormBuilder);
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly roteador = inject(Router);

  readonly retorno = input<string>();

  readonly formulario = this.construtor.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
  });

  readonly enviando = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly senhaVisivel = signal(false);

  alternarSenha(): void {
    this.senhaVisivel.update((visivel) => !visivel);
  }

  entrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    if (this.enviando()) {
      return;
    }

    this.enviando.set(true);
    this.erroGeral.set(null);

    this.autenticacao.entrar(this.formulario.getRawValue()).subscribe({
      next: () => void this.roteador.navigateByUrl(destinoSeguro(this.retorno())),
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.erroGeral.set(descreverErro(erro));
      },
    });
  }
}
