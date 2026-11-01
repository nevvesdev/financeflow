package br.com.nevvesdev.financeflow.painel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContasAVencerResposta(
    LocalDate ate,
    long quantidade,
    BigDecimal valorTotal,
    List<ContaAVencerResposta> itens) {
}
