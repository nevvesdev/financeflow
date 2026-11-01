package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.compartilhado.NaoAutenticadoException;
import br.com.nevvesdev.financeflow.compartilhado.RecursoNaoEncontradoException;
import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import br.com.nevvesdev.financeflow.usuario.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConsultaDeUsoDeCategoria consultaDeUso;

    public CategoriaService(
        CategoriaRepository categoriaRepository,
        UsuarioRepository usuarioRepository,
        ConsultaDeUsoDeCategoria consultaDeUso) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.consultaDeUso = consultaDeUso;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResposta> listar(UUID usuarioId, TipoLancamento tipo) {
        return categoriaRepository.findAllByUsuarioIdOrderByNomeAsc(usuarioId).stream()
            .filter(categoria -> tipo == null || categoria.getTipo() == tipo)
            .map(CategoriaResposta::de)
            .toList();
    }

    @Transactional
    public CategoriaResposta criar(UUID usuarioId, String nome, TipoLancamento tipo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new NaoAutenticadoException("Usuário não encontrado"));
        try {
            return CategoriaResposta.de(categoriaRepository.saveAndFlush(Categoria.criar(usuario, nome, tipo)));
        } catch (DataIntegrityViolationException excecao) {
            throw new CategoriaDuplicadaException();
        }
    }

    @Transactional
    public CategoriaResposta renomear(UUID usuarioId, UUID id, String novoNome) {
        Categoria categoria = buscar(usuarioId, id);
        categoria.renomear(novoNome);
        try {
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException excecao) {
            throw new CategoriaDuplicadaException();
        }
        return CategoriaResposta.de(categoria);
    }

    @Transactional
    public void excluir(UUID usuarioId, UUID id) {
        Categoria categoria = buscar(usuarioId, id);
        if (consultaDeUso.estaEmUso(id)) {
            throw new CategoriaEmUsoException();
        }
        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException excecao) {
            throw new CategoriaEmUsoException();
        }
    }

    private Categoria buscar(UUID usuarioId, UUID id) {
        return categoriaRepository.findByIdAndUsuarioId(id, usuarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }
}
