package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;
import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LancamentoResposta(
    UUID id,
    String descricao,
    BigDecimal valor,
    TipoLancamento tipo,
    NaturezaLancamento natureza,
    UUID categoriaId,
    String categoriaNome,
    LocalDate dataLancamento,
    LocalDate dataVencimento,
    boolean paga,
    LocalDate dataPagamento,
    boolean vencido,
    Instant criadoEm,
    Instant atualizadoEm) {

    public static LancamentoResposta de(Lancamento lancamento, LocalDate hoje) {
        return new LancamentoResposta(
            lancamento.getId(),
            lancamento.getDescricao(),
            lancamento.getValor(),
            lancamento.getTipo(),
            lancamento.getNatureza(),
            lancamento.getCategoria().getId(),
            lancamento.getCategoria().getNome(),
            lancamento.getDataLancamento(),
            lancamento.getDataVencimento(),
            lancamento.isPaga(),
            lancamento.getDataPagamento(),
            lancamento.estaVencido(hoje),
            lancamento.getCriadoEm(),
            lancamento.getAtualizadoEm());
    }
}
