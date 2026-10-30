package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.BasePersistenciaTest;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import br.com.nevvesdev.financeflow.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static br.com.nevvesdev.financeflow.compartilhado.TipoLancamento.DESPESA;
import static br.com.nevvesdev.financeflow.compartilhado.TipoLancamento.RECEITA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoriaRepositoryTest extends BasePersistenciaTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Usuario joao;
    private Usuario maria;

    @BeforeEach
    void preparar() {
        joao = usuarioRepository.save(Usuario.criar("João", "joao@financeflow.dev", "hash"));
        maria = usuarioRepository.save(Usuario.criar("Maria", "maria@financeflow.dev", "hash"));
    }

    @Test
    void deveListarApenasCategoriasDoUsuarioOrdenadasPorNome() {
        categoriaRepository.save(Categoria.criar(joao, "Transporte", DESPESA));
        categoriaRepository.save(Categoria.criar(joao, "Alimentação", DESPESA));
        categoriaRepository.save(Categoria.criar(maria, "Lazer", DESPESA));
        sincronizar();

        List<Categoria> categorias = categoriaRepository.findAllByUsuarioIdOrderByNomeAsc(joao.getId());

        assertThat(categorias).extracting(Categoria::getNome).containsExactly("Alimentação", "Transporte");
    }

    @Test
    void deveVerificarExistenciaIgnorandoCaixa() {
        categoriaRepository.save(Categoria.criar(joao, "Moradia", DESPESA));
        sincronizar();

        assertThat(categoriaRepository.existsByUsuarioIdAndTipoAndNomeIgnoreCase(joao.getId(), DESPESA, "moradia"))
            .isTrue();
        assertThat(categoriaRepository.existsByUsuarioIdAndTipoAndNomeIgnoreCase(joao.getId(), RECEITA, "moradia"))
            .isFalse();
        assertThat(categoriaRepository.existsByUsuarioIdAndTipoAndNomeIgnoreCase(maria.getId(), DESPESA, "moradia"))
            .isFalse();
    }

    @Test
    void deveImpedirCategoriaDuplicadaIgnorandoCaixa() {
        categoriaRepository.saveAndFlush(Categoria.criar(joao, "Moradia", DESPESA));

        assertThatThrownBy(() -> categoriaRepository.saveAndFlush(Categoria.criar(joao, "MORADIA", DESPESA)))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void devePermitirMesmoNomeParaUsuariosDiferentes() {
        categoriaRepository.saveAndFlush(Categoria.criar(joao, "Moradia", DESPESA));

        Categoria outra = categoriaRepository.saveAndFlush(Categoria.criar(maria, "Moradia", DESPESA));

        assertThat(outra.getId()).isNotNull();
    }

    @Test
    void naoDeveBuscarCategoriaDeOutroUsuario() {
        Categoria categoria = categoriaRepository.saveAndFlush(Categoria.criar(joao, "Moradia", DESPESA));
        sincronizar();

        assertThat(categoriaRepository.findByIdAndUsuarioId(categoria.getId(), joao.getId())).isPresent();
        assertThat(categoriaRepository.findByIdAndUsuarioId(categoria.getId(), maria.getId())).isEmpty();
    }
}
