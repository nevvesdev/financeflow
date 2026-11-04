export interface LoginRequisicao {
  email: string;
  senha: string;
}

export interface CadastroRequisicao {
  nome: string;
  email: string;
  senha: string;
}

export interface TokenResposta {
  tokenAcesso: string;
  tipo: string;
  expiraEm: string;
}

export interface Sessao {
  tokenAcesso: string;
  expiraEm: string;
}

export interface Usuario {
  id: string;
  nome: string;
  email: string;
}
