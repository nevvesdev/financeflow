import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { descreverErro, errosPorCampo } from '../../core/api/erro-api';
import { AutenticacaoService } from '../../core/autenticacao/autenticacao.service';
import { Marca } from '../../shared/marca/marca';
import { camposIguais } from '../../shared/validadores';

type Campo = 'nome' | 'email' | 'senha' | 'confirmacao';

@Component({
  selector: 'app-pagina-cadastro',
  imports: [ReactiveFormsModule, RouterLink, Marca],
  templateUrl: './pagina-cadastro.html',
  styleUrl: './autenticacao.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaginaCadastro {
  private readonly construtor = inject(NonNullableFormBuilder);
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly roteador = inject(Router);

  readonly formulario = this.construtor.group(
    {
      nome: ['', [Validators.required, Validators.maxLength(120)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
      senha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmacao: ['', [Validators.required]],
    },
    { validators: camposIguais('senha', 'confirmacao') },
  );

  readonly enviando = signal(false);
  readonly erroGeral = signal<string | null>(null);
  readonly senhaVisivel = signal(false);

  alternarSenha(): void {
    this.senhaVisivel.update((visivel) => !visivel);
  }

  mensagemDoCampo(campo: Campo): string | null {
    const controle = this.formulario.controls[campo];
    if (!controle.touched) {
      return null;
    }
    const erros = controle.errors;
    if (erros !== null) {
      if (erros['servidor']) {
        return String(erros['servidor']);
      }
      if (erros['required']) {
        return 'Campo obrigatório.';
      }
      if (erros['email']) {
        return 'Informe um e-mail válido.';
      }
      if (erros['minlength']) {
        return 'A senha deve ter pelo menos 8 caracteres.';
      }
      if (erros['maxlength']) {
        return 'Texto longo demais para este campo.';
      }
    }
    if (campo === 'confirmacao' && this.formulario.hasError('camposDiferentes')) {
      return 'As senhas não conferem.';
    }
    return null;
  }

  cadastrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    if (this.enviando()) {
      return;
    }

    this.enviando.set(true);
    this.erroGeral.set(null);
    const { nome, email, senha } = this.formulario.getRawValue();

    this.autenticacao.cadastrar({ nome, email, senha }).subscribe({
      next: () => void this.roteador.navigateByUrl('/painel'),
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.aplicarErros(erro);
      },
    });
  }

  private aplicarErros(erro: unknown): void {
    const errosDoServidor = Object.entries(errosPorCampo(erro));
    for (const [campo, mensagem] of errosDoServidor) {
      const controle = this.formulario.get(campo);
      controle?.setErrors({ servidor: mensagem });
      controle?.markAsTouched();
    }
    this.erroGeral.set(errosDoServidor.length > 0 ? null : descreverErro(erro));
  }
}
