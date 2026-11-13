export function valorDoEvento(evento: Event): string {
  return (evento.target as HTMLInputElement | HTMLSelectElement).value;
}

export function proximoIndice(atual: number, total: number, tecla: string): number | null {
  switch (tecla) {
    case 'ArrowDown':
    case 'ArrowRight':
      return (atual + 1) % total;
    case 'ArrowUp':
    case 'ArrowLeft':
      return (atual - 1 + total) % total;
    case 'Home':
      return 0;
    case 'End':
      return total - 1;
    default:
      return null;
  }
}