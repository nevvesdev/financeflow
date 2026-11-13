import { TipoLancamento } from '../../core/api/dominio';

export interface Categoria {
  id: string;
  nome: string;
  tipo: TipoLancamento;
}