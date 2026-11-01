package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;
import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

public final class LancamentoEspecificacoes {

    private static final char CARACTERE_DE_ESCAPE = '\\';

    private LancamentoEspecificacoes() {
    }

    public static Specification<Lancamento> comFiltro(UUID usuarioId, FiltroLancamentos filtro, LocalDate hoje) {
        Specification<Lancamento> especificacao = doUsuario(usuarioId);
        if (filtro.inicio() != null) {
            especificacao = especificacao.and(lancadoAPartirDe(filtro.inicio()));
        }
        if (filtro.fim() != null) {
            especificacao = especificacao.and(lancadoAte(filtro.fim()));
        }
        if (filtro.tipo() != null) {
            especificacao = especificacao.and(doTipo(filtro.tipo()));
        }
        if (filtro.natureza() != null) {
            especificacao = especificacao.and(daNatureza(filtro.natureza()));
        }
        if (filtro.categoriaId() != null) {
            especificacao = especificacao.and(daCategoria(filtro.categoriaId()));
        }
        if (filtro.paga() != null) {
            especificacao = especificacao.and(comPagamento(filtro.paga()));
        }
        if (Boolean.TRUE.equals(filtro.vencidos())) {
            especificacao = especificacao.and(vencidos(hoje));
        }
        if (filtro.busca() != null && !filtro.busca().isBlank()) {
            especificacao = especificacao.and(comDescricaoContendo(filtro.busca()));
        }
        return especificacao;
    }

    private static Specification<Lancamento> doUsuario(UUID usuarioId) {
        return (raiz, consulta, construtor) ->
            construtor.equal(raiz.get("usuario").get("id"), usuarioId);
    }

    private static Specification<Lancamento> lancadoAPartirDe(LocalDate inicio) {
        return (raiz, consulta, construtor) ->
            construtor.greaterThanOrEqualTo(raiz.<LocalDate>get("dataLancamento"), inicio);
    }

    private static Specification<Lancamento> lancadoAte(LocalDate fim) {
        return (raiz, consulta, construtor) ->
            construtor.lessThanOrEqualTo(raiz.<LocalDate>get("dataLancamento"), fim);
    }

    private static Specification<Lancamento> doTipo(TipoLancamento tipo) {
        return (raiz, consulta, construtor) -> construtor.equal(raiz.get("tipo"), tipo);
    }

    private static Specification<Lancamento> daNatureza(NaturezaLancamento natureza) {
        return (raiz, consulta, construtor) -> construtor.equal(raiz.get("natureza"), natureza);
    }

    private static Specification<Lancamento> daCategoria(UUID categoriaId) {
        return (raiz, consulta, construtor) ->
            construtor.equal(raiz.get("categoria").get("id"), categoriaId);
    }

    private static Specification<Lancamento> comPagamento(boolean paga) {
        return (raiz, consulta, construtor) -> construtor.equal(raiz.<Boolean>get("paga"), paga);
    }

    private static Specification<Lancamento> vencidos(LocalDate hoje) {
        return (raiz, consulta, construtor) -> construtor.and(
            construtor.isFalse(raiz.<Boolean>get("paga")),
            construtor.isNotNull(raiz.get("dataVencimento")),
            construtor.lessThan(raiz.<LocalDate>get("dataVencimento"), hoje));
    }

    private static Specification<Lancamento> comDescricaoContendo(String busca) {
        String padrao = "%" + escapar(busca.strip().toLowerCase(Locale.ROOT)) + "%";
        return (raiz, consulta, construtor) ->
            construtor.like(construtor.lower(raiz.<String>get("descricao")), padrao, CARACTERE_DE_ESCAPE);
    }

    private static String escapar(String texto) {
        return texto
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }
}
