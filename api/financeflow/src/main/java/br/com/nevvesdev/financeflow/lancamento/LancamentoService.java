package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.categoria.Categoria;
import br.com.nevvesdev.financeflow.categoria.CategoriaRepository;
import br.com.nevvesdev.financeflow.compartilhado.NaoAutenticadoException;
import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;
import br.com.nevvesdev.financeflow.compartilhado.RecursoNaoEncontradoException;
import br.com.nevvesdev.financeflow.compartilhado.RegraDeNegocioException;
import br.com.nevvesdev.financeflow.compartilhado.web.PaginaResposta;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import br.com.nevvesdev.financeflow.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
public class LancamentoService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private static final Set<String> CAMPOS_ORDENAVEIS =
        Set.of("dataLancamento", "dataVencimento", "valor", "descricao", "criadoEm");

    private static final Sort ORDENACAO_PADRAO =
        Sort.by(Sort.Order.desc("dataLancamento"), Sort.Order.desc("criadoEm"));

    private final LancamentoRepository lancamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock relogio;

    public LancamentoService(
        LancamentoRepository lancamentoRepository,
        CategoriaRepository categoriaRepository,
        UsuarioRepository usuarioRepository,
        Clock relogio) {
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.relogio = relogio;
    }

    @Transactional
    public LancamentoResposta criar(UUID usuarioId, NovoLancamentoRequisicao requisicao) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new NaoAutenticadoException("Usuário não encontrado"));
        Categoria categoria = buscarCategoria(usuarioId, requisicao.categoriaId());
        NaturezaLancamento natureza =
            requisicao.natureza() != null ? requisicao.natureza() : NaturezaLancamento.VARIAVEL;

        Lancamento lancamento = Lancamento.criar(
            usuario,
            categoria,
            requisicao.descricao(),
            requisicao.valor(),
            requisicao.dataLancamento(),
            requisicao.dataVencimento(),
            natureza);
        if (requisicao.dataPagamento() != null) {
            lancamento.marcarComoPaga(requisicao.dataPagamento());
        }
        return responder(lancamentoRepository.saveAndFlush(lancamento));
    }

    @Transactional(readOnly = true)
    public LancamentoResposta buscar(UUID usuarioId, UUID id) {
        return responder(buscarDoUsuario(usuarioId, id));
    }

    @Transactional(readOnly = true)
    public PaginaResposta<LancamentoResposta> listar(
        UUID usuarioId, FiltroLancamentos filtro, Pageable paginacao) {
        filtro.validar();
        LocalDate hoje = hoje();
        Page<LancamentoResposta> pagina = lancamentoRepository
            .findAll(LancamentoEspecificacoes.comFiltro(usuarioId, filtro, hoje), paginacaoSegura(paginacao))
            .map(lancamento -> LancamentoResposta.de(lancamento, hoje));
        return PaginaResposta.de(pagina);
    }

    @Transactional
    public LancamentoResposta atualizar(UUID usuarioId, UUID id, AlteracaoLancamentoRequisicao requisicao) {
        Lancamento lancamento = buscarDoUsuario(usuarioId, id);
        Categoria categoria = buscarCategoria(usuarioId, requisicao.categoriaId());
        lancamento.atualizar(
            categoria,
            requisicao.descricao(),
            requisicao.valor(),
            requisicao.dataLancamento(),
            requisicao.dataVencimento());
        if (requisicao.natureza() != null) {
            lancamento.definirNatureza(requisicao.natureza());
        }
        lancamentoRepository.flush();
        return responder(lancamento);
    }

    @Transactional
    public void excluir(UUID usuarioId, UUID id) {
        lancamentoRepository.delete(buscarDoUsuario(usuarioId, id));
    }

    @Transactional
    public LancamentoResposta marcarComoPaga(UUID usuarioId, UUID id, LocalDate dataPagamento) {
        Lancamento lancamento = buscarDoUsuario(usuarioId, id);
        lancamento.marcarComoPaga(dataPagamento != null ? dataPagamento : hoje());
        lancamentoRepository.flush();
        return responder(lancamento);
    }

    @Transactional
    public LancamentoResposta desfazerPagamento(UUID usuarioId, UUID id) {
        Lancamento lancamento = buscarDoUsuario(usuarioId, id);
        lancamento.desfazerPagamento();
        lancamentoRepository.flush();
        return responder(lancamento);
    }

    private Lancamento buscarDoUsuario(UUID usuarioId, UUID id) {
        return lancamentoRepository.findByIdAndUsuarioId(id, usuarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento não encontrado"));
    }

    private Categoria buscarCategoria(UUID usuarioId, UUID categoriaId) {
        return categoriaRepository.findByIdAndUsuarioId(categoriaId, usuarioId)
            .orElseThrow(() -> new RegraDeNegocioException("categoria não encontrada"));
    }

    private Pageable paginacaoSegura(Pageable paginacao) {
        paginacao.getSort().forEach(ordem -> {
            if (!CAMPOS_ORDENAVEIS.contains(ordem.getProperty())) {
                throw new RegraDeNegocioException("ordenação não suportada: " + ordem.getProperty());
            }
        });
        Sort ordenacao = paginacao.getSort().isSorted() ? paginacao.getSort() : ORDENACAO_PADRAO;
        return PageRequest.of(
            paginacao.getPageNumber(),
            Math.min(paginacao.getPageSize(), TAMANHO_MAXIMO_PAGINA),
            ordenacao.and(Sort.by("id")));
    }

    private LancamentoResposta responder(Lancamento lancamento) {
        return LancamentoResposta.de(lancamento, hoje());
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio);
    }
}
