package br.com.nevvesdev.financeflow.lancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContaParaAviso(
    UUID lancamentoId,
    UUID usuarioId,
    String descricao,
    BigDecimal valor,
    LocalDate dataVencimento) {
}
