import { paraIso } from './datas';
import { formatarData } from './formatadores';

export interface Periodo {
  inicio: string | null;
  fim: string | null;
}

export type PresetPeriodo =
  | 'este-mes'
  | 'mes-passado'
  | 'ultimos-30-dias'
  | 'este-ano'
  | 'todo-periodo';

export interface OpcaoPeriodo {
  id: PresetPeriodo;
  rotulo: string;
}

export const PRESETS: readonly OpcaoPeriodo[] = [
  { id: 'este-mes', rotulo: 'Este mês' },
  { id: 'mes-passado', rotulo: 'Mês passado' },
  { id: 'ultimos-30-dias', rotulo: 'Últimos 30 dias' },
  { id: 'este-ano', rotulo: 'Este ano' },
  { id: 'todo-periodo', rotulo: 'Todo o período' },
];

export function calcularPeriodo(preset: PresetPeriodo, hoje: Date = new Date()): Periodo {
  const ano = hoje.getFullYear();
  const mes = hoje.getMonth();

  switch (preset) {
    case 'este-mes':
      return { inicio: paraIso(new Date(ano, mes, 1)), fim: paraIso(new Date(ano, mes + 1, 0)) };
    case 'mes-passado':
      return { inicio: paraIso(new Date(ano, mes - 1, 1)), fim: paraIso(new Date(ano, mes, 0)) };
    case 'ultimos-30-dias':
      return { inicio: paraIso(new Date(ano, mes, hoje.getDate() - 29)), fim: paraIso(hoje) };
    case 'este-ano':
      return { inicio: paraIso(new Date(ano, 0, 1)), fim: paraIso(new Date(ano, 11, 31)) };
    case 'todo-periodo':
      return { inicio: null, fim: null };
  }
}

export function mesmoPeriodo(a: Periodo, b: Periodo): boolean {
  return a.inicio === b.inicio && a.fim === b.fim;
}

export function rotuloDoPeriodo(periodo: Periodo, hoje: Date = new Date()): string {
  const preset = PRESETS.find((opcao) => mesmoPeriodo(calcularPeriodo(opcao.id, hoje), periodo));
  if (preset) {
    return preset.rotulo;
  }
  if (periodo.inicio && periodo.fim) {
    return `${formatarData(periodo.inicio)} – ${formatarData(periodo.fim)}`;
  }
  if (periodo.inicio) {
    return `A partir de ${formatarData(periodo.inicio)}`;
  }
  if (periodo.fim) {
    return `Até ${formatarData(periodo.fim)}`;
  }
  return 'Todo o período';
}