package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    List<Categoria> findAllByUsuarioIdOrderByNomeAsc(UUID usuarioId);

    Optional<Categoria> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByUsuarioIdAndTipoAndNomeIgnoreCase(UUID usuarioId, TipoLancamento tipo, String nome);
}
