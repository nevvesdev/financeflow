const NOMES_DOS_MESES = [
  'Janeiro',
  'Fevereiro',
  'Março',
  'Abril',
  'Maio',
  'Junho',
  'Julho',
  'Agosto',
  'Setembro',
  'Outubro',
  'Novembro',
  'Dezembro',
];

export function mesAtual(): string {
  const agora = new Date();
  return montar(agora.getFullYear(), agora.getMonth() + 1);
}

export function somarMeses(mes: string, deslocamento: number): string {
  const [ano, numero] = dividir(mes);
  const indice = ano * 12 + (numero - 1) + deslocamento;
  return montar(Math.floor(indice / 12), (indice % 12) + 1);
}

export function nomeDoMes(mes: string): string {
  const [ano, numero] = dividir(mes);
  return `${NOMES_DOS_MESES[numero - 1]} de ${ano}`;
}

function dividir(mes: string): [number, number] {
  const [ano, numero] = mes.split('-').map(Number);
  return [ano, numero];
}

function montar(ano: number, numero: number): string {
  return `${ano}-${String(numero).padStart(2, '0')}`;
}
