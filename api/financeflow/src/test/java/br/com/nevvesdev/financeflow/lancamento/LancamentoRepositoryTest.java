package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.BasePersistenciaTest;
import br.com.nevvesdev.financeflow.categoria.Categoria;
import br.com.nevvesdev.financeflow.categoria.CategoriaRepository;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import br.com.nevvesdev.financeflow.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static br.com.nevvesdev.financeflow.compartilhado.TipoLancamento.DESPESA;
import static org.assertj.core.api.Assertions.assertThat;

class LancamentoRepositoryTest extends BasePersistenciaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 30);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    private Usuario joao;
    private Usuario maria;
    private Categoria moradiaDoJoao;
    private Categoria moradiaDaMaria;

    @BeforeEach
    void preparar() {
        joao = usuarioRepository.save(Usuario.criar("João", "joao@financeflow.dev", "hash"));
        maria = usuarioRepository.save(Usuario.criar("Maria", "maria@financeflow.dev", "hash"));
        moradiaDoJoao = categoriaRepository.save(Categoria.criar(joao, "Moradia", DESPESA));
        moradiaDaMaria = categoriaRepository.save(Categoria.criar(maria, "Moradia", DESPESA));
    }

    @Test
    void devePersistirLancamentoComValorEPagamento() {
        Lancamento lancamento = Lancamento.criar(
            joao, moradiaDoJoao, "Aluguel", new BigDecimal("1234.5"), HOJE, HOJE.plusDays(3));
        lancamento.marcarComoPaga(HOJE);
        UUID id = lancamentoRepository.saveAndFlush(lancamento).getId();
        sincronizar();

        Lancamento salvo = lancamentoRepository.findById(id).orElseThrow();

        assertThat(salvo.getValor()).isEqualTo(new BigDecimal("1234.50"));
        assertThat(salvo.getTipo()).isEqualTo(DESPESA);
        assertThat(salvo.isPaga()).isTrue();
        assertThat(salvo.getDataPagamento()).isEqualTo(HOJE);
        assertThat(salvo.getCategoria().getNome()).isEqualTo("Moradia");
        assertThat(salvo.getCriadoEm()).isNotNull();
        assertThat(salvo.getAtualizadoEm()).isNotNull();
    }

    @Test
    void deveBuscarApenasLancamentosEmAbertoComVencimentoAteOLimite() {
        salvar(joao, moradiaDoJoao, "Próximo", HOJE.plusDays(3));
        salvar(joao, moradiaDoJoao, "Vencido", HOJE.minusDays(2));
        salvar(joao, moradiaDoJoao, "Distante", HOJE.plusDays(10));
        salvar(joao, moradiaDoJoao, "Sem vencimento", null);
        Lancamento pago = salvar(joao, moradiaDoJoao, "Pago", HOJE.plusDays(1));
        pago.marcarComoPaga(HOJE);
        salvar(maria, moradiaDaMaria, "De outra pessoa", HOJE.plusDays(1));
        sincronizar();

        List<Lancamento> resultado =
            lancamentoRepository.buscarAbertosComVencimentoAte(joao.getId(), HOJE.plusDays(7));

        assertThat(resultado).extracting(Lancamento::getDescricao).containsExactly("Vencido", "Próximo");
    }

    @Test
    void deveBuscarLancamentoApenasDoProprietario() {
        Lancamento lancamento = salvar(joao, moradiaDoJoao, "Aluguel", null);
        sincronizar();

        assertThat(lancamentoRepository.findByIdAndUsuarioId(lancamento.getId(), joao.getId())).isPresent();
        assertThat(lancamentoRepository.findByIdAndUsuarioId(lancamento.getId(), maria.getId())).isEmpty();
    }

    private Lancamento salvar(Usuario usuario, Categoria categoria, String descricao, LocalDate vencimento) {
        return lancamentoRepository.save(
            Lancamento.criar(usuario, categoria, descricao, new BigDecimal("100.00"), HOJE, vencimento));
    }
}
