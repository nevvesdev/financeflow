package br.com.nevvesdev.financeflow.recorrencia;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record NovaRecorrenciaRequisicao(
    @NotBlank @Size(max = 160) String descricao,
    @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal valorEstimado,
    @NotNull UUID categoriaId,
    @NotNull @Min(1) @Max(31) Integer diaVencimento,
    LocalDate dataInicio,
    LocalDate dataFim) {
}
