package br.com.nevvesdev.financeflow.compartilhado.web;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaResposta<T>(
    List<T> conteudo,
    int pagina,
    int tamanho,
    long totalElementos,
    int totalPaginas) {

    public static <T> PaginaResposta<T> de(Page<T> pagina) {
        return new PaginaResposta<>(
            pagina.getContent(),
            pagina.getNumber(),
            pagina.getSize(),
            pagina.getTotalElements(),
            pagina.getTotalPages());
    }
}
