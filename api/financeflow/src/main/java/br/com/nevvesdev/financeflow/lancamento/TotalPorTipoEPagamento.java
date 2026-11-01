package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;

import java.math.BigDecimal;

public record TotalPorTipoEPagamento(TipoLancamento tipo, Boolean paga, BigDecimal total) {
}
