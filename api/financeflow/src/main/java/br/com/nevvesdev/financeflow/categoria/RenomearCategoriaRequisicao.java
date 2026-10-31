package br.com.nevvesdev.financeflow.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenomearCategoriaRequisicao(@NotBlank @Size(max = 60) String nome) {
}
