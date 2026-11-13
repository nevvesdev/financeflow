export interface Totais {
  entradas: number;
  saidas: number;
  saldo: number;
}

export interface ContaAVencer {
  id: string;
  descricao: string;
  valor: number;
  categoriaNome: string;
  natureza: 'FIXA' | 'VARIAVEL';
  dataVencimento: string;
  diasParaVencer: number;
  vencido: boolean;
}

export interface ContasAVencer {
  ate: string;
  quantidade: number;
  valorTotal: number;
  itens: ContaAVencer[];
}

export interface GastoPorCategoria {
  categoriaId: string;
  categoriaNome: string;
  total: number;
  percentual: number;
}

export interface DespesasPorNatureza {
  fixas: number;
  variaveis: number;
}

export interface Painel {
  mes: string;
  inicio: string;
  fim: string;
  previsto: Totais;
  realizado: Totais;
  contasAVencer: ContasAVencer;
  gastosPorCategoria: GastoPorCategoria[];
  despesasPorNatureza: DespesasPorNatureza;
}
