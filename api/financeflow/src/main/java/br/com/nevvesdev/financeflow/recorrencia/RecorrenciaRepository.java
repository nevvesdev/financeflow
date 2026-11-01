package br.com.nevvesdev.financeflow.recorrencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecorrenciaRepository extends JpaRepository<Recorrencia, UUID> {

    Optional<Recorrencia> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    List<Recorrencia> findAllByUsuarioIdOrderByDescricaoAsc(UUID usuarioId);

    boolean existsByCategoriaId(UUID categoriaId);

    @Query("select r.id from Recorrencia r where r.ativa = true")
    List<UUID> buscarIdsAtivas();
}
