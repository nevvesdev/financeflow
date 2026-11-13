const MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const PERCENTUAL = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 1,
});
const MESES_ABREVIADOS = [
  'jan',
  'fev',
  'mar',
  'abr',
  'mai',
  'jun',
  'jul',
  'ago',
  'set',
  'out',
  'nov',
  'dez',
] as const;

export function formatarMoeda(valor: number): string {
  return MOEDA.format(valor === 0 ? 0 : valor);
}

export function formatarPercentual(valor: number): string {
  return `${PERCENTUAL.format(valor)}%`;
}

export function formatarData(iso: string): string {
  const [ano, mes, dia] = iso.split('-');
  return `${dia}/${mes}/${ano}`;
}

export function formatarDataCurta(iso: string): string {
  const [, mes, dia] = iso.split('-');
  return `${dia} ${MESES_ABREVIADOS[Number(mes) - 1]}`;
}

export function descreverPrazo(dias: number): string {
  if (dias === 0) {
    return 'Vence hoje';
  }
  if (dias === 1) {
    return 'Vence amanhã';
  }
  if (dias > 1) {
    return `Vence em ${dias} dias`;
  }
  if (dias === -1) {
    return 'Venceu ontem';
  }
  return `Venceu há ${Math.abs(dias)} dias`;
}
