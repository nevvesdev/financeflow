const PADRAO = '/painel';
const ROTAS_DE_VISITANTE = ['/login', '/cadastro'];

export function destinoSeguro(retorno: string | undefined, padrao: string = PADRAO): string {
  if (
    retorno === undefined ||
    !retorno.startsWith('/') ||
    retorno.startsWith('//') ||
    retorno.startsWith('/\\') ||
    ROTAS_DE_VISITANTE.some((rota) => retorno.startsWith(rota))
  ) {
    return padrao;
  }
  return retorno;
}
