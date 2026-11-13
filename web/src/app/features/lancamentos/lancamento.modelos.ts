import { NaturezaLancamento, TipoLancamento } from '../../core/api/dominio';

export interface Lancamento {
  id: string;
  descricao: string;
  valor: number;
  tipo: TipoLancamento;
  natureza: NaturezaLancamento;
  categoriaId: string;
  categoriaNome: string;
  dataLancamento: string;
  dataVencimento: string | null;
  paga: boolean;
  dataPagamento: string | null;
  vencido: boolean;
  criadoEm: string;
  atualizadoEm: string;
}

export interface Pagina<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface FiltroLancamentos {
  tipo?: TipoLancamento;
  natureza?: NaturezaLancamento;
  categoriaId?: string;
  paga?: boolean;
  vencidos?: boolean;
  busca?: string;
  inicio?: string;
  fim?: string;
}

export type CampoOrdenacao = 'dataLancamento' | 'dataVencimento' | 'valor' | 'descricao';

export interface Ordenacao {
  campo: CampoOrdenacao;
  direcao: 'asc' | 'desc';
}