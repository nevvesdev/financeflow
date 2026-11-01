package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;

import java.math.BigDecimal;

public record TotalPorNatureza(NaturezaLancamento natureza, BigDecimal total) {
}
