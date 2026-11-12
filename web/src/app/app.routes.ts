import { Routes } from '@angular/router';

import { autenticadoGuard, visitanteGuard } from './core/autenticacao/autenticacao.guard';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Entrar · FinanceFlow',
    canActivate: [visitanteGuard],
    loadComponent: () =>
      import('./features/autenticacao/pagina-login').then((modulo) => modulo.PaginaLogin),
  },
  {
    path: 'cadastro',
    title: 'Criar conta · FinanceFlow',
    canActivate: [visitanteGuard],
    loadComponent: () =>
      import('./features/autenticacao/pagina-cadastro').then((modulo) => modulo.PaginaCadastro),
  },
  {
    path: '',
    canActivate: [autenticadoGuard],
    loadComponent: () => import('./layout/layout-principal').then((modulo) => modulo.LayoutPrincipal),
    children: [
      {
        path: 'painel',
        title: 'Painel · FinanceFlow',
        loadComponent: () =>
          import('./features/painel/pagina-painel').then((modulo) => modulo.PaginaPainel),
      },
      { path: '', pathMatch: 'full', redirectTo: 'painel' },
    ],
  },
  { path: '**', redirectTo: '' },
];
