package br.com.nevvesdev.financeflow.painel;

import java.time.LocalDate;
import java.util.List;

public record PainelResposta(
    String mes,
    LocalDate inicio,
    LocalDate fim,
    TotaisResposta previsto,
    TotaisResposta realizado,
    ContasAVencerResposta contasAVencer,
    List<GastoPorCategoriaResposta> gastosPorCategoria,
    DespesasPorNaturezaResposta despesasPorNatureza) {
}
