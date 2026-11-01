package br.com.nevvesdev.financeflow.notificacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    Optional<Notificacao> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByLancamentoIdAndTipo(UUID lancamentoId, TipoNotificacao tipo);

    long countByUsuarioIdAndLidaEmIsNull(UUID usuarioId);

    @Query(value = """
            select n
            from Notificacao n
            where n.usuarioId = :usuarioId
              and (:apenasNaoLidas = false or n.lidaEm is null)
            order by n.criadaEm desc, n.id desc
            """,
        countQuery = """
                    select count(n)
                    from Notificacao n
                    where n.usuarioId = :usuarioId
                      and (:apenasNaoLidas = false or n.lidaEm is null)
                    """)
    Page<Notificacao> listar(
        @Param("usuarioId") UUID usuarioId,
        @Param("apenasNaoLidas") boolean apenasNaoLidas,
        Pageable paginacao);

    @Query("select n.id from Notificacao n where n.enviadaEm is null order by n.criadaEm asc, n.id asc")
    List<UUID> buscarIdsPendentesDeEnvio(Pageable paginacao);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notificacao n set n.lidaEm = :agora where n.usuarioId = :usuarioId and n.lidaEm is null")
    int marcarTodasComoLidas(@Param("usuarioId") UUID usuarioId, @Param("agora") Instant agora);
}
