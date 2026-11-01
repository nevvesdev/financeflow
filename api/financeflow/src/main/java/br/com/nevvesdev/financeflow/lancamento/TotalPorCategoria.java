package br.com.nevvesdev.financeflow.lancamento;

import java.math.BigDecimal;
import java.util.UUID;

public record TotalPorCategoria(UUID categoriaId, String categoriaNome, BigDecimal total) {
}
