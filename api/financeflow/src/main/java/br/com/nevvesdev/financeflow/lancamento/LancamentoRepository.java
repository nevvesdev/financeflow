package br.com.nevvesdev.financeflow.lancamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LancamentoRepository extends JpaRepository<Lancamento, UUID> {

    Optional<Lancamento> findByIdAndUsuarioId(UUID id, UUID usuarioId);

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
}
