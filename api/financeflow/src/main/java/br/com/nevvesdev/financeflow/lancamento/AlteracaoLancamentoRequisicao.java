package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AlteracaoLancamentoRequisicao(
    @NotBlank @Size(max = 160) String descricao,
    @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal valor,
    @NotNull UUID categoriaId,
    @NotNull LocalDate dataLancamento,
    LocalDate dataVencimento,
    NaturezaLancamento natureza) {
}
