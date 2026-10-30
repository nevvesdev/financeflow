package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.categoria.Categoria;
import br.com.nevvesdev.financeflow.compartilhado.RegraDeNegocioException;
import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LancamentoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 30);

    private Usuario usuario;
    private Categoria moradia;

    @BeforeEach
    void preparar() {
        usuario = Usuario.criar("João Neves", "joao@financeflow.dev", "hash");
        moradia = Categoria.criar(usuario, "Moradia", TipoLancamento.DESPESA);
    }

    @Test
    void deveHerdarOTipoDaCategoria() {
        Lancamento lancamento = novo("1500", HOJE.plusDays(5));

        assertThat(lancamento.getTipo()).isEqualTo(TipoLancamento.DESPESA);
        assertThat(lancamento.isPaga()).isFalse();
        assertThat(lancamento.getDataPagamento()).isNull();
    }

    @Test
    void deveNormalizarOValorParaDuasCasasDecimais() {
        assertThat(novo("10.5", null).getValor()).isEqualTo(new BigDecimal("10.50"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    void deveRejeitarValorNaoPositivo(String valor) {
        assertThatThrownBy(() -> novo(valor, null))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("valor deve ser maior que zero");
    }

    @Test
    void deveRejeitarValorComMaisDeDuasCasasDecimais() {
        assertThatThrownBy(() -> novo("10.505", null))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("valor deve ter no máximo duas casas decimais");
    }

    @Test
    void deveRejeitarDescricaoEmBranco() {
        assertThatThrownBy(() -> Lancamento.criar(usuario, moradia, "  ", BigDecimal.TEN, HOJE, null))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("campo obrigatório: descrição");
    }

    @Test
    void deveMarcarComoPaga() {
        Lancamento lancamento = novo("100", HOJE);

        lancamento.marcarComoPaga(HOJE);

        assertThat(lancamento.isPaga()).isTrue();
        assertThat(lancamento.getDataPagamento()).isEqualTo(HOJE);
    }

    @Test
    void naoDeveMarcarComoPagaDuasVezes() {
        Lancamento lancamento = novo("100", HOJE);
        lancamento.marcarComoPaga(HOJE);

        assertThatThrownBy(() -> lancamento.marcarComoPaga(HOJE))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("lançamento já está pago");
    }

    @Test
    void deveDesfazerPagamento() {
        Lancamento lancamento = novo("100", HOJE);
        lancamento.marcarComoPaga(HOJE);

        lancamento.desfazerPagamento();

        assertThat(lancamento.isPaga()).isFalse();
        assertThat(lancamento.getDataPagamento()).isNull();
    }

    @Test
    void naoDeveDesfazerPagamentoDeLancamentoEmAberto() {
        Lancamento lancamento = novo("100", HOJE);

        assertThatThrownBy(lancamento::desfazerPagamento)
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("lançamento não está pago");
    }

    @Test
    void deveEstarVencidoQuandoEmAbertoEComPrazoAnteriorAHoje() {
        assertThat(novo("100", HOJE.minusDays(1)).estaVencido(HOJE)).isTrue();
    }

    @Test
    void naoDeveEstarVencidoNoDiaDoVencimentoOuSemVencimento() {
        assertThat(novo("100", HOJE).estaVencido(HOJE)).isFalse();
        assertThat(novo("100", null).estaVencido(HOJE)).isFalse();
    }

    @Test
    void naoDeveEstarVencidoQuandoPago() {
        Lancamento lancamento = novo("100", HOJE.minusDays(1));
        lancamento.marcarComoPaga(HOJE);

        assertThat(lancamento.estaVencido(HOJE)).isFalse();
    }

    @Test
    void naoDeveTrocarParaCategoriaDeOutroTipo() {
        Lancamento lancamento = novo("100", null);
        Categoria salario = Categoria.criar(usuario, "Salário", TipoLancamento.RECEITA);

        assertThatThrownBy(() -> lancamento.atualizar(salario, "Aluguel", BigDecimal.TEN, HOJE, null))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("categoria deve ser do mesmo tipo do lançamento");
    }

    private Lancamento novo(String valor, LocalDate vencimento) {
        return Lancamento.criar(usuario, moradia, "Aluguel", new BigDecimal(valor), HOJE, vencimento);
    }
}
