package br.com.nevvesdev.financeflow.usuario;

import br.com.nevvesdev.financeflow.BasePersistenciaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioRepositoryTest extends BasePersistenciaTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveBuscarUsuarioPorEmail() {
        usuarioRepository.saveAndFlush(Usuario.criar("Maria", "maria@financeflow.dev", "hash"));
        sincronizar();

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("maria@financeflow.dev");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isNotNull();
        assertThat(encontrado.get().getCriadoEm()).isNotNull();
    }

    @Test
    void deveInformarSeEmailJaExiste() {
        usuarioRepository.saveAndFlush(Usuario.criar("Maria", "maria@financeflow.dev", "hash"));

        assertThat(usuarioRepository.existsByEmail("maria@financeflow.dev")).isTrue();
        assertThat(usuarioRepository.existsByEmail("outro@financeflow.dev")).isFalse();
    }

    @Test
    void deveRejeitarEmailDuplicado() {
        usuarioRepository.saveAndFlush(Usuario.criar("Maria", "maria@financeflow.dev", "hash"));

        assertThatThrownBy(() ->
            usuarioRepository.saveAndFlush(Usuario.criar("Outra Maria", "maria@financeflow.dev", "hash")))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
