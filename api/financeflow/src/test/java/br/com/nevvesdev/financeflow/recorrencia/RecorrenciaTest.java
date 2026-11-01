package br.com.nevvesdev.financeflow.recorrencia;

import br.com.nevvesdev.financeflow.categoria.Categoria;
import br.com.nevvesdev.financeflow.compartilhado.RegraDeNegocioException;
import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecorrenciaTest {

    private static final YearMonth NOVEMBRO = YearMonth.of(2026, 11);
    private static final YearMonth DEZEMBRO = YearMonth.of(2026, 12);

    private Usuario usuario;
    private Categoria moradia;

    @BeforeEach
    void preparar() {
        usuario = Usuario.criar("João Neves", "joao@financeflow.dev", "hash");
        moradia = Categoria.criar(usuario, "Moradia", TipoLancamento.DESPESA);
    }

    @Test
    void deveLimitarOVencimentoAoUltimoDiaDoMes() {
        Recorrencia recorrencia = nova(31, LocalDate.of(2027, 1, 1), null);

        assertThat(recorrencia.vencimentoEm(YearMonth.of(2027, 1))).isEqualTo(LocalDate.of(2027, 1, 31));
        assertThat(recorrencia.vencimentoEm(YearMonth.of(2027, 2))).isEqualTo(LocalDate.of(2027, 2, 28));
        assertThat(recorrencia.vencimentoEm(YearMonth.of(2028, 2))).isEqualTo(LocalDate.of(2028, 2, 29));
        assertThat(recorrencia.vencimentoEm(YearMonth.of(2027, 4))).isEqualTo(LocalDate.of(2027, 4, 30));
    }

    @Test
    void deveGerarOMesAtualEOProximoQuandoInicioJaPassou() {
        Recorrencia recorrencia = nova(5, LocalDate.of(2026, 1, 10), null);

        assertThat(recorrencia.competenciasPendentes(NOVEMBRO, DEZEMBRO)).containsExactly(NOVEMBRO, DEZEMBRO);
    }

    @Test
    void naoDeveGerarQuandoInicioEstaAlemDoHorizonte() {
        Recorrencia recorrencia = nova(5, LocalDate.of(2027, 2, 1), null);

        assertThat(recorrencia.competenciasPendentes(NOVEMBRO, DEZEMBRO)).isEmpty();
    }

    @Test
    void deveComecarNoMesDeInicioQuandoFuturoDentroDoHorizonte() {
        Recorrencia recorrencia = nova(5, LocalDate.of(2026, 12, 1), null);

        assertThat(recorrencia.competenciasPendentes(NOVEMBRO, DEZEMBRO)).containsExactly(DEZEMBRO);
    }

    @Test
    void deveRespeitarADataFinal() {
        Recorrencia recorrencia = nova(5, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 11, 30));

        assertThat(recorrencia.competenciasPendentes(NOVEMBRO, DEZEMBRO)).containsExactly(NOVEMBRO);
    }

    @Test
    void deveContinuarDepoisDaUltimaCompetenciaGerada() {
        Recorrencia recorrencia = nova(5, LocalDate.of(2026, 1, 10), null);
        recorrencia.registrarGeracao(NOVEMBRO);

        assertThat(recorrencia.competenciasPendentes(NOVEMBRO, DEZEMBRO)).containsExactly(DEZEMBRO);

        recorrencia.registrarGeracao(DEZEMBRO);

        assertThat(recorrencia.competenciasPendentes(NOVEMBRO, DEZEMBRO)).isEmpty();
    }

    @Test
    void naoDeveRetroagirDepoisDeUmaPausaLonga() {
        Recorrencia recorrencia = nova(5, LocalDate.of(2026, 1, 10), null);
        recorrencia.registrarGeracao(YearMonth.of(2026, 6));

        assertThat(recorrencia.competenciasPendentes(NOVEMBRO, DEZEMBRO)).containsExactly(NOVEMBRO, DEZEMBRO);
    }

    @Test
    void naoDeveGerarQuandoPausada() {
        Recorrencia recorrencia = nova(5, LocalDate.of(2026, 1, 10), null);
        recorrencia.pausar();

        assertThat(recorrencia.competenciasPendentes(NOVEMBRO, DEZEMBRO)).isEmpty();
    }

    @Test
    void naoDevePausarNemRetomarDuasVezes() {
        Recorrencia recorrencia = nova(5, LocalDate.of(2026, 1, 10), null);

        assertThatThrownBy(recorrencia::retomar)
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("recorrência já está ativa");

        recorrencia.pausar();

        assertThatThrownBy(recorrencia::pausar)
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("recorrência já está pausada");
    }

    @Test
    void deveRejeitarDiaForaDoIntervalo() {
        assertThatThrownBy(() -> nova(0, LocalDate.of(2026, 1, 1), null))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("dia de vencimento deve estar entre 1 e 31");
        assertThatThrownBy(() -> nova(32, LocalDate.of(2026, 1, 1), null))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("dia de vencimento deve estar entre 1 e 31");
    }

    @Test
    void deveRejeitarDataFinalAnteriorAInicial() {
        assertThatThrownBy(() -> nova(5, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 30)))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("data final não pode ser anterior à data inicial");
    }

    @Test
    void deveRejeitarValorInvalido() {
        assertThatThrownBy(() -> Recorrencia.criar(
            usuario, moradia, "Aluguel", BigDecimal.ZERO, 5, LocalDate.of(2026, 1, 1), null))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("valor deve ser maior que zero");
    }

    @Test
    void naoDeveTrocarParaCategoriaDeOutroTipo() {
        Recorrencia recorrencia = nova(5, LocalDate.of(2026, 1, 1), null);
        Categoria salario = Categoria.criar(usuario, "Salário", TipoLancamento.RECEITA);

        assertThatThrownBy(() -> recorrencia.atualizar(salario, "Aluguel", BigDecimal.TEN, 5, null))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("categoria deve ser do mesmo tipo da recorrência");
    }

    private Recorrencia nova(int dia, LocalDate inicio, LocalDate fim) {
        return Recorrencia.criar(usuario, moradia, "Aluguel", new BigDecimal("1500.00"), dia, inicio, fim);
    }
}
