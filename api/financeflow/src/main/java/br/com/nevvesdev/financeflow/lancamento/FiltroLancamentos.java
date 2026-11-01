package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;
import br.com.nevvesdev.financeflow.compartilhado.RegraDeNegocioException;
import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

public record FiltroLancamentos(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
    TipoLancamento tipo,
    NaturezaLancamento natureza,
    UUID categoriaId,
    Boolean paga,
    Boolean vencidos,
    String busca) {

    public void validar() {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RegraDeNegocioException("o início do período não pode ser posterior ao fim");
        }
    }
}
