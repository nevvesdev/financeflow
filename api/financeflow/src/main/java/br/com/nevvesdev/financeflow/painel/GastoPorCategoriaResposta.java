package br.com.nevvesdev.financeflow.painel;

import java.math.BigDecimal;
import java.util.UUID;

public record GastoPorCategoriaResposta(
    UUID categoriaId,
    String categoriaNome,
    BigDecimal total,
    BigDecimal percentual) {
}
