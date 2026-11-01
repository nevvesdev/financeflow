package br.com.nevvesdev.financeflow.painel;

import br.com.nevvesdev.financeflow.BaseApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PainelControllerTest extends BaseApiTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private String token;
    private String mercado;
    private String combustivel;
    private String freelance;

    @BeforeEach
    void preparar() throws Exception {
        token = registrarEAutenticar("maria@financeflow.dev");
        mercado = criarCategoria(token, "Mercado", "DESPESA");
        combustivel = criarCategoria(token, "Combustivel", "DESPESA");
        freelance = criarCategoria(token, "Freelance", "RECEITA");
    }

    @Test
    void deveCalcularSaldoPrevistoERealizadoDoMes() throws Exception {
        lancar("Projeto A", "1000.00", freelance, "2026-10-05", null, null, "2026-10-05");
        lancar("Projeto B", "500.00", freelance, "2026-10-20", null, null, null);
        lancar("Feira", "300.00", mercado, "2026-10-10", null, null, "2026-10-10");
        lancar("Compra grande", "200.00", mercado, "2026-10-25", null, null, null);
        lancar("Mes anterior", "999.00", mercado, "2026-09-30", null, null, null);

        painel(token, "2026-10")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mes").value("2026-10"))
            .andExpect(jsonPath("$.inicio").value("2026-10-01"))
            .andExpect(jsonPath("$.fim").value("2026-10-31"))
            .andExpect(jsonPath("$.previsto.entradas").value(1500.0))
            .andExpect(jsonPath("$.previsto.saidas").value(500.0))
            .andExpect(jsonPath("$.previsto.saldo").value(1000.0))
            .andExpect(jsonPath("$.realizado.entradas").value(1000.0))
            .andExpect(jsonPath("$.realizado.saidas").value(300.0))
            .andExpect(jsonPath("$.realizado.saldo").value(700.0));
    }

    @Test
    void deveAgruparGastosPorCategoriaComPercentual() throws Exception {
        lancar("Feira", "300.00", mercado, "2026-10-10", null, null, null);
        lancar("Gasolina", "100.00", combustivel, "2026-10-12", null, null, null);
        lancar("Projeto", "900.00", freelance, "2026-10-15", null, null, null);

        painel(token, "2026-10")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.gastosPorCategoria.length()").value(2))
            .andExpect(jsonPath("$.gastosPorCategoria[0].categoriaNome").value("Mercado"))
            .andExpect(jsonPath("$.gastosPorCategoria[0].total").value(300.0))
            .andExpect(jsonPath("$.gastosPorCategoria[0].percentual").value(75.0))
            .andExpect(jsonPath("$.gastosPorCategoria[1].categoriaNome").value("Combustivel"))
            .andExpect(jsonPath("$.gastosPorCategoria[1].percentual").value(25.0));
    }

    @Test
    void deveSepararDespesasFixasEVariaveis() throws Exception {
        lancar("Aluguel", "1500.00", mercado, "2026-10-05", null, "FIXA", null);
        lancar("Sushi", "40.00", mercado, "2026-10-18", null, null, null);
        lancar("Projeto", "900.00", freelance, "2026-10-15", null, "FIXA", null);

        painel(token, "2026-10")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.despesasPorNatureza.fixas").value(1500.0))
            .andExpect(jsonPath("$.despesasPorNatureza.variaveis").value(40.0));
    }

    @Test
    void deveListarContasAVencerNosProximosSeteDiasComAsAtrasadasPrimeiro() throws Exception {
        LocalDate hoje = LocalDate.now(FUSO);
        lancar("Vencida", "100.00", mercado, hoje.toString(), hoje.minusDays(3).toString(), null, null);
        lancar("Vence hoje", "50.00", mercado, hoje.toString(), hoje.toString(), null, null);
        lancar("Vence em sete dias", "25.00", mercado, hoje.toString(), hoje.plusDays(7).toString(), null, null);
        lancar("Fora da janela", "10.00", mercado, hoje.toString(), hoje.plusDays(8).toString(), null, null);
        lancar("Ja paga", "40.00", mercado, hoje.toString(), hoje.plusDays(1).toString(), null, hoje.toString());
        lancar("Receita aberta", "70.00", freelance, hoje.toString(), hoje.plusDays(2).toString(), null, null);

        painel(token, null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contasAVencer.ate").value(hoje.plusDays(7).toString()))
            .andExpect(jsonPath("$.contasAVencer.quantidade").value(3))
            .andExpect(jsonPath("$.contasAVencer.valorTotal").value(175.0))
            .andExpect(jsonPath("$.contasAVencer.itens.length()").value(3))
            .andExpect(jsonPath("$.contasAVencer.itens[0].descricao").value("Vencida"))
            .andExpect(jsonPath("$.contasAVencer.itens[0].vencido").value(true))
            .andExpect(jsonPath("$.contasAVencer.itens[0].diasParaVencer").value(-3))
            .andExpect(jsonPath("$.contasAVencer.itens[1].vencido").value(false))
            .andExpect(jsonPath("$.contasAVencer.itens[1].diasParaVencer").value(0))
            .andExpect(jsonPath("$.contasAVencer.itens[2].diasParaVencer").value(7));
    }

    @Test
    void deveUsarOMesAtualQuandoNaoInformado() throws Exception {
        painel(token, null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mes").value(YearMonth.now(FUSO).toString()));
    }

    @Test
    void deveRetornarPainelZeradoParaUsuarioSemLancamentos() throws Exception {
        lancar("Feira", "300.00", mercado, "2026-10-10", null, null, null);
        String outroToken = registrarEAutenticar("joao@financeflow.dev");

        painel(outroToken, "2026-10")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.previsto.entradas").value(0.0))
            .andExpect(jsonPath("$.previsto.saidas").value(0.0))
            .andExpect(jsonPath("$.previsto.saldo").value(0.0))
            .andExpect(jsonPath("$.gastosPorCategoria.length()").value(0))
            .andExpect(jsonPath("$.despesasPorNatureza.fixas").value(0.0))
            .andExpect(jsonPath("$.contasAVencer.quantidade").value(0))
            .andExpect(jsonPath("$.contasAVencer.itens.length()").value(0));
    }

    @Test
    void deveRejeitarMesInvalido() throws Exception {
        painel(token, "2026-13")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void naoDeveAcessarSemToken() throws Exception {
        mockMvc.perform(get("/api/painel"))
            .andExpect(status().isUnauthorized());
    }

    private ResultActions painel(String tokenDoUsuario, String mes) throws Exception {
        MockHttpServletRequestBuilder requisicao = get("/api/painel")
            .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario));
        if (mes != null) {
            requisicao.param("mes", mes);
        }
        return mockMvc.perform(requisicao);
    }

    private void lancar(
        String descricao,
        String valor,
        String categoriaId,
        String dataLancamento,
        String dataVencimento,
        String natureza,
        String dataPagamento) throws Exception {
        StringBuilder corpo = new StringBuilder()
            .append("{\"descricao\": \"").append(descricao).append("\", ")
            .append("\"valor\": ").append(valor).append(", ")
            .append("\"categoriaId\": \"").append(categoriaId).append("\", ")
            .append("\"dataLancamento\": \"").append(dataLancamento).append("\"");
        if (dataVencimento != null) {
            corpo.append(", \"dataVencimento\": \"").append(dataVencimento).append("\"");
        }
        if (natureza != null) {
            corpo.append(", \"natureza\": \"").append(natureza).append("\"");
        }
        if (dataPagamento != null) {
            corpo.append(", \"dataPagamento\": \"").append(dataPagamento).append("\"");
        }
        corpo.append("}");

        mockMvc.perform(post("/api/lancamentos")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo.toString()))
            .andExpect(status().isCreated());
    }
}
