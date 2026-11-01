package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LancamentoRepository
    extends JpaRepository<Lancamento, UUID>, JpaSpecificationExecutor<Lancamento> {

    Optional<Lancamento> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByCategoriaId(UUID categoriaId);

    @Query("""
            select l
            from Lancamento l
            join fetch l.categoria
            where l.usuario.id = :usuarioId
              and l.paga = false
              and l.dataVencimento is not null
              and l.dataVencimento <= :limite
            order by l.dataVencimento asc, l.criadoEm asc
            """)
    List<Lancamento> buscarAbertosComVencimentoAte(
        @Param("usuarioId") UUID usuarioId,
        @Param("limite") LocalDate limite);

    @Query("""
            select new br.com.nevvesdev.financeflow.lancamento.TotalPorTipoEPagamento(
                l.tipo, l.paga, sum(l.valor))
            from Lancamento l
            where l.usuario.id = :usuarioId
              and l.dataLancamento between :inicio and :fim
            group by l.tipo, l.paga
            """)
    List<TotalPorTipoEPagamento> totalizarPorTipoEPagamento(
        @Param("usuarioId") UUID usuarioId,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);

    @Query("""
            select new br.com.nevvesdev.financeflow.lancamento.TotalPorCategoria(
                c.id, c.nome, sum(l.valor))
            from Lancamento l
            join l.categoria c
            where l.usuario.id = :usuarioId
              and l.tipo = :tipo
              and l.dataLancamento between :inicio and :fim
            group by c.id, c.nome
            order by sum(l.valor) desc, c.nome asc
            """)
    List<TotalPorCategoria> totalizarPorCategoria(
        @Param("usuarioId") UUID usuarioId,
        @Param("tipo") TipoLancamento tipo,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);

    @Query("""
            select new br.com.nevvesdev.financeflow.lancamento.TotalPorNatureza(
                l.natureza, sum(l.valor))
            from Lancamento l
            where l.usuario.id = :usuarioId
              and l.tipo = :tipo
              and l.dataLancamento between :inicio and :fim
            group by l.natureza
            """)
    List<TotalPorNatureza> totalizarPorNatureza(
        @Param("usuarioId") UUID usuarioId,
        @Param("tipo") TipoLancamento tipo,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);

    @Query("""
            select l
            from Lancamento l
            join fetch l.categoria
            where l.usuario.id = :usuarioId
              and l.tipo = :tipo
              and l.paga = false
              and l.dataVencimento is not null
              and l.dataVencimento <= :limite
            order by l.dataVencimento asc, l.valor desc, l.id asc
            """)
    List<Lancamento> buscarContasAbertasAte(
        @Param("usuarioId") UUID usuarioId,
        @Param("tipo") TipoLancamento tipo,
        @Param("limite") LocalDate limite,
        Pageable paginacao);

    @Query("""
            select new br.com.nevvesdev.financeflow.lancamento.TotalDeContasAbertas(
                count(l), sum(l.valor))
            from Lancamento l
            where l.usuario.id = :usuarioId
              and l.tipo = :tipo
              and l.paga = false
              and l.dataVencimento is not null
              and l.dataVencimento <= :limite
            """)
    TotalDeContasAbertas totalizarContasAbertasAte(
        @Param("usuarioId") UUID usuarioId,
        @Param("tipo") TipoLancamento tipo,
        @Param("limite") LocalDate limite);
}
