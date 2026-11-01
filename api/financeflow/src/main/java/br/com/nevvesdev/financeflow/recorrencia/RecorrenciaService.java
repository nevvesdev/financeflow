package br.com.nevvesdev.financeflow.recorrencia;

import br.com.nevvesdev.financeflow.categoria.Categoria;
import br.com.nevvesdev.financeflow.categoria.CategoriaRepository;
import br.com.nevvesdev.financeflow.compartilhado.NaoAutenticadoException;
import br.com.nevvesdev.financeflow.compartilhado.RecursoNaoEncontradoException;
import br.com.nevvesdev.financeflow.compartilhado.RegraDeNegocioException;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import br.com.nevvesdev.financeflow.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RecorrenciaService {

    private final RecorrenciaRepository recorrenciaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final GeradorDeLancamentos gerador;
    private final Clock relogio;

    public RecorrenciaService(
        RecorrenciaRepository recorrenciaRepository,
        CategoriaRepository categoriaRepository,
        UsuarioRepository usuarioRepository,
        GeradorDeLancamentos gerador,
        Clock relogio) {
        this.recorrenciaRepository = recorrenciaRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.gerador = gerador;
        this.relogio = relogio;
    }

    @Transactional
    public RecorrenciaResposta criar(UUID usuarioId, NovaRecorrenciaRequisicao requisicao) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new NaoAutenticadoException("Usuário não encontrado"));
        Categoria categoria = buscarCategoria(usuarioId, requisicao.categoriaId());
        LocalDate inicio = requisicao.dataInicio() != null ? requisicao.dataInicio() : LocalDate.now(relogio);

        Recorrencia recorrencia = recorrenciaRepository.saveAndFlush(Recorrencia.criar(
            usuario,
            categoria,
            requisicao.descricao(),
            requisicao.valorEstimado(),
            requisicao.diaVencimento(),
            inicio,
            requisicao.dataFim()));
        gerador.gerar(recorrencia.getId());
        return RecorrenciaResposta.de(recorrencia);
    }

    @Transactional(readOnly = true)
    public List<RecorrenciaResposta> listar(UUID usuarioId, Boolean ativa) {
        return recorrenciaRepository.findAllByUsuarioIdOrderByDescricaoAsc(usuarioId).stream()
            .filter(recorrencia -> ativa == null || recorrencia.isAtiva() == ativa)
            .map(RecorrenciaResposta::de)
            .toList();
    }

    @Transactional(readOnly = true)
    public RecorrenciaResposta buscar(UUID usuarioId, UUID id) {
        return RecorrenciaResposta.de(buscarDoUsuario(usuarioId, id));
    }

    @Transactional
    public RecorrenciaResposta atualizar(UUID usuarioId, UUID id, AlteracaoRecorrenciaRequisicao requisicao) {
        Recorrencia recorrencia = buscarDoUsuario(usuarioId, id);
        Categoria categoria = buscarCategoria(usuarioId, requisicao.categoriaId());
        recorrencia.atualizar(
            categoria,
            requisicao.descricao(),
            requisicao.valorEstimado(),
            requisicao.diaVencimento(),
            requisicao.dataFim());
        recorrenciaRepository.flush();
        return RecorrenciaResposta.de(recorrencia);
    }

    @Transactional
    public RecorrenciaResposta pausar(UUID usuarioId, UUID id) {
        Recorrencia recorrencia = buscarDoUsuario(usuarioId, id);
        recorrencia.pausar();
        recorrenciaRepository.flush();
        return RecorrenciaResposta.de(recorrencia);
    }

    @Transactional
    public RecorrenciaResposta retomar(UUID usuarioId, UUID id) {
        Recorrencia recorrencia = buscarDoUsuario(usuarioId, id);
        recorrencia.retomar();
        gerador.gerar(recorrencia.getId());
        recorrenciaRepository.flush();
        return RecorrenciaResposta.de(recorrencia);
    }

    @Transactional
    public void excluir(UUID usuarioId, UUID id) {
        recorrenciaRepository.delete(buscarDoUsuario(usuarioId, id));
    }

    private Recorrencia buscarDoUsuario(UUID usuarioId, UUID id) {
        return recorrenciaRepository.findByIdAndUsuarioId(id, usuarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Recorrência não encontrada"));
    }

    private Categoria buscarCategoria(UUID usuarioId, UUID categoriaId) {
        return categoriaRepository.findByIdAndUsuarioId(categoriaId, usuarioId)
            .orElseThrow(() -> new RegraDeNegocioException("categoria não encontrada"));
    }
}
