package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;

import java.util.UUID;

public record CategoriaResposta(UUID id, String nome, TipoLancamento tipo) {

    public static CategoriaResposta de(Categoria categoria) {
        return new CategoriaResposta(categoria.getId(), categoria.getNome(), categoria.getTipo());
    }
}
