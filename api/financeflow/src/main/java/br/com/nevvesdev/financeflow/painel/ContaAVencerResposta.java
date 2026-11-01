package br.com.nevvesdev.financeflow.painel;

import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;
import br.com.nevvesdev.financeflow.lancamento.Lancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record ContaAVencerResposta(
    UUID id,
    String descricao,
    BigDecimal valor,
    String categoriaNome,
    NaturezaLancamento natureza,
    LocalDate dataVencimento,
    long diasParaVencer,
    boolean vencido) {

    public static ContaAVencerResposta de(Lancamento lancamento, LocalDate hoje) {
        return new ContaAVencerResposta(
            lancamento.getId(),
            lancamento.getDescricao(),
            lancamento.getValor(),
            lancamento.getCategoria().getNome(),
            lancamento.getNatureza(),
            lancamento.getDataVencimento(),
            ChronoUnit.DAYS.between(hoje, lancamento.getDataVencimento()),
            lancamento.estaVencido(hoje));
    }
}
