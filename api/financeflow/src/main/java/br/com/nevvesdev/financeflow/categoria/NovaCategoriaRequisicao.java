package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NovaCategoriaRequisicao(
    @NotBlank @Size(max = 60) String nome,
    @NotNull TipoLancamento tipo) {
}
