package br.com.nevvesdev.financeflow.recorrencia;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

public record RecorrenciaResposta(
    UUID id,
    String descricao,
    BigDecimal valorEstimado,
    TipoLancamento tipo,
    UUID categoriaId,
    String categoriaNome,
    int diaVencimento,
    LocalDate dataInicio,
    LocalDate dataFim,
    boolean ativa,
    String ultimaCompetenciaGerada) {

    public static RecorrenciaResposta de(Recorrencia recorrencia) {
        LocalDate ultima = recorrencia.getUltimaCompetenciaGerada();
        return new RecorrenciaResposta(
            recorrencia.getId(),
            recorrencia.getDescricao(),
            recorrencia.getValorEstimado(),
            recorrencia.getCategoria().getTipo(),
            recorrencia.getCategoria().getId(),
            recorrencia.getCategoria().getNome(),
            recorrencia.getDiaVencimento(),
            recorrencia.getDataInicio(),
            recorrencia.getDataFim(),
            recorrencia.isAtiva(),
            ultima != null ? YearMonth.from(ultima).toString() : null);
    }
}
