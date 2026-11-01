package br.com.nevvesdev.financeflow.recorrencia;

import br.com.nevvesdev.financeflow.BaseApiTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecorrenciaControllerTest extends BaseApiTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Autowired
    private AgendadorDeRecorrencias agendador;

    private String token;
    private String categoriaAluguel;

    @BeforeEach
    void preparar() throws Exception {
        token = registrarEAutenticar("maria@financeflow.dev");
        categoriaAluguel = criarCategoria(token, "Aluguel", "DESPESA");
    }

    @Test
    void deveCriarRecorrenciaEGerarLancamentosDoMesAtualEDoProximo() throws Exception {
        YearMonth mesAtual = YearMonth.now(FUSO);
        YearMonth proximoMes = mesAtual.plusMonths(1);

        criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, 5))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.tipo").value("DESPESA"))
            .andExpect(jsonPath("$.ativa").value(true))
            .andExpect(jsonPath("$.ultimaCompetenciaGerada").value(proximoMes.toString()));

        lancamentosFixos(token)
            .andExpect(jsonPath("$.totalElementos").value(2))
            .andExpect(jsonPath("$.conteudo[0].dataVencimento").value(proximoMes.atDay(5).toString()))
            .andExpect(jsonPath("$.conteudo[1].dataVencimento").value(mesAtual.atDay(5).toString()))
            .andExpect(jsonPath("$.conteudo[0].valor").value(1500.0))
            .andExpect(jsonPath("$.conteudo[0].natureza").value("FIXA"))
            .andExpect(jsonPath("$.conteudo[0].paga").value(false));
    }

    @Test
    void deveAjustarDiaInexistenteParaOUltimoDiaDoMes() throws Exception {
        YearMonth mesAtual = YearMonth.now(FUSO);

        criar(token, corpo("Condominio", "800.00", categoriaAluguel, 31))
            .andExpect(status().isCreated());

        lancamentosFixos(token)
            .andExpect(jsonPath("$.conteudo[1].dataVencimento")
                .value(mesAtual.atDay(Math.min(31, mesAtual.lengthOfMonth())).toString()));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 32})
    void deveRejeitarDiaDeVencimentoInvalido(int dia) throws Exception {
        criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, dia))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erros.diaVencimento").exists());
    }

    @Test
    void deveRejeitarCategoriaDeOutroUsuario() throws Exception {
        String outroToken = registrarEAutenticar("joao@financeflow.dev");
        String categoriaDoOutro = criarCategoria(outroToken, "Aluguel", "DESPESA");

        criar(token, corpo("Aluguel", "1500.00", categoriaDoOutro, 5))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarEFiltrarPorStatus() throws Exception {
        String id = idDe(criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, 5)));

        acao("pausar", id).andExpect(status().isOk());

        mockMvc.perform(get("/api/recorrencias").param("ativa", "false")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/recorrencias").param("ativa", "true")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void devePausarERetomarSemRepetirAAcao() throws Exception {
        String id = idDe(criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, 5)));

        acao("pausar", id)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ativa").value(false));
        acao("pausar", id).andExpect(status().isBadRequest());

        acao("retomar", id)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ativa").value(true));
        acao("retomar", id).andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarSemAlterarOQueJaFoiGerado() throws Exception {
        String id = idDe(criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, 5)));

        mockMvc.perform(put("/api/recorrencias/" + id)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"descricao": "Aluguel novo", "valorEstimado": 1600.00,
                                 "categoriaId": "%s", "diaVencimento": 10}
                                """.formatted(categoriaAluguel)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.descricao").value("Aluguel novo"))
            .andExpect(jsonPath("$.valorEstimado").value(1600.0))
            .andExpect(jsonPath("$.diaVencimento").value(10));

        lancamentosFixos(token)
            .andExpect(jsonPath("$.conteudo[0].valor").value(1500.0));
    }

    @Test
    void deveExcluirMantendoOsLancamentosJaGerados() throws Exception {
        String id = idDe(criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, 5)));

        mockMvc.perform(delete("/api/recorrencias/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/recorrencias/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isNotFound());

        lancamentosFixos(token)
            .andExpect(jsonPath("$.totalElementos").value(2));
    }

    @Test
    void naoDeveAcessarRecorrenciaDeOutroUsuario() throws Exception {
        String id = idDe(criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, 5)));
        String outroToken = registrarEAutenticar("joao@financeflow.dev");

        mockMvc.perform(get("/api/recorrencias/" + id).header(HttpHeaders.AUTHORIZATION, bearer(outroToken)))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/recorrencias/" + id + "/pausar")
                .header(HttpHeaders.AUTHORIZATION, bearer(outroToken)))
            .andExpect(status().isNotFound());
    }

    @Test
    void naoDeveExcluirCategoriaUsadaPorRecorrencia() throws Exception {
        criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, 5))
            .andExpect(status().isCreated());
        excluirTodosOsLancamentos(token);

        mockMvc.perform(delete("/api/categorias/" + categoriaAluguel)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isConflict());
    }

    @Test
    void deveSerIdempotenteAoExecutarOJobNovamente() throws Exception {
        criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, 5))
            .andExpect(status().isCreated());

        agendador.gerarLancamentos();
        agendador.gerarLancamentos();

        lancamentosFixos(token)
            .andExpect(jsonPath("$.totalElementos").value(2));
    }

    @Test
    void naoDeveRegerarLancamentoExcluidoPeloUsuario() throws Exception {
        criar(token, corpo("Aluguel", "1500.00", categoriaAluguel, 5))
            .andExpect(status().isCreated());

        String resposta = lancamentosFixos(token)
            .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(resposta, "$.conteudo[*].id");
        mockMvc.perform(delete("/api/lancamentos/" + ids.get(0)).header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isNoContent());

        agendador.gerarLancamentos();

        lancamentosFixos(token)
            .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void naoDeveListarSemToken() throws Exception {
        mockMvc.perform(get("/api/recorrencias"))
            .andExpect(status().isUnauthorized());
    }

    private ResultActions criar(String tokenDoUsuario, String corpo) throws Exception {
        return mockMvc.perform(post("/api/recorrencias")
            .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario))
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo));
    }

    private ResultActions acao(String acao, String id) throws Exception {
        return mockMvc.perform(post("/api/recorrencias/" + id + "/" + acao)
            .header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private ResultActions lancamentosFixos(String tokenDoUsuario) throws Exception {
        return mockMvc.perform(get("/api/lancamentos")
            .param("natureza", "FIXA")
            .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario)));
    }

    private void excluirTodosOsLancamentos(String tokenDoUsuario) throws Exception {
        String resposta = lancamentosFixos(tokenDoUsuario)
            .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(resposta, "$.conteudo[*].id");
        for (String id : ids) {
            mockMvc.perform(delete("/api/lancamentos/" + id)
                    .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario)))
                .andExpect(status().isNoContent());
        }
    }

    private String idDe(ResultActions resultado) throws Exception {
        String resposta = resultado
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private static String corpo(String descricao, String valor, String categoriaId, int dia) {
        return """
                {"descricao": "%s", "valorEstimado": %s, "categoriaId": "%s", "diaVencimento": %d}
                """.formatted(descricao, valor, categoriaId, dia);
    }
}
