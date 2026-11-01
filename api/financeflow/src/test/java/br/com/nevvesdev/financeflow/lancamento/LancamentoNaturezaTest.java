package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.categoria.Categoria;
import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;
import br.com.nevvesdev.financeflow.compartilhado.RegraDeNegocioException;
import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LancamentoNaturezaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 31);

    private Usuario usuario;
    private Categoria moradia;

    @BeforeEach
    void preparar() {
        usuario = Usuario.criar("João Neves", "joao@financeflow.dev", "hash");
        moradia = Categoria.criar(usuario, "Moradia", TipoLancamento.DESPESA);
    }

    @Test
    void deveIniciarComoVariavelPorPadrao() {
        Lancamento lancamento = Lancamento.criar(usuario, moradia, "Reparo", BigDecimal.TEN, HOJE, null);

        assertThat(lancamento.getNatureza()).isEqualTo(NaturezaLancamento.VARIAVEL);
    }

    @Test
    void deveCriarComNaturezaInformada() {
        Lancamento lancamento = Lancamento.criar(
            usuario, moradia, "Aluguel", BigDecimal.TEN, HOJE, HOJE, NaturezaLancamento.FIXA);

        assertThat(lancamento.getNatureza()).isEqualTo(NaturezaLancamento.FIXA);
    }

    @Test
    void deveAlterarNatureza() {
        Lancamento lancamento = Lancamento.criar(usuario, moradia, "Internet", BigDecimal.TEN, HOJE, null);

        lancamento.definirNatureza(NaturezaLancamento.FIXA);

        assertThat(lancamento.getNatureza()).isEqualTo(NaturezaLancamento.FIXA);
    }

    @Test
    void deveRejeitarNaturezaNula() {
        assertThatThrownBy(() -> Lancamento.criar(usuario, moradia, "Aluguel", BigDecimal.TEN, HOJE, null, null))
            .isInstanceOf(RegraDeNegocioException.class)
            .hasMessage("campo obrigatório: natureza");
    }
}
