package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.BaseApiTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LancamentoControllerTest extends BaseApiTest {

    private String token;
    private String categoriaDespesa;
    private String categoriaReceita;

    @BeforeEach
    void preparar() throws Exception {
        token = registrarEAutenticar("maria@financeflow.dev");
        categoriaDespesa = criarCategoria(token, "Mercado", "DESPESA");
        categoriaReceita = criarCategoria(token, "Freelance", "RECEITA");
    }

    @Test
    void deveCriarLancamentoComTipoDaCategoriaENaturezaPadrao() throws Exception {
        criar(token, corpo("Feira", "25.50", categoriaDespesa, "2026-10-31"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.tipo").value("DESPESA"))
            .andExpect(jsonPath("$.natureza").value("VARIAVEL"))
            .andExpect(jsonPath("$.valor").value(25.5))
            .andExpect(jsonPath("$.categoriaNome").value("Mercado"))
            .andExpect(jsonPath("$.paga").value(false))
            .andExpect(jsonPath("$.vencido").value(false));
    }

    @Test
    void deveCriarContaFixaVencida() throws Exception {
        criar(token, """
                {"descricao": "Aluguel", "valor": 1500.00, "categoriaId": "%s",
                 "dataLancamento": "2020-01-01", "dataVencimento": "2020-01-10", "natureza": "FIXA"}
                """.formatted(categoriaDespesa))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.natureza").value("FIXA"))
            .andExpect(jsonPath("$.vencido").value(true));
    }

    @Test
    void deveCriarLancamentoJaPago() throws Exception {
        criar(token, """
                {"descricao": "Conta de luz", "valor": 180.40, "categoriaId": "%s",
                 "dataLancamento": "2026-10-20", "dataVencimento": "2026-10-25", "dataPagamento": "2026-10-24"}
                """.formatted(categoriaDespesa))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.paga").value(true))
            .andExpect(jsonPath("$.dataPagamento").value("2026-10-24"))
            .andExpect(jsonPath("$.vencido").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "10.505"})
    void deveRejeitarValorInvalido(String valor) throws Exception {
        criar(token, """
                {"descricao": "Teste", "valor": %s, "categoriaId": "%s", "dataLancamento": "2026-10-31"}
                """.formatted(valor, categoriaDespesa))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erros.valor").exists());
    }

    @Test
    void deveRejeitarCategoriaDeOutroUsuario() throws Exception {
        String outroToken = registrarEAutenticar("joao@financeflow.dev");
        String categoriaDoOutro = criarCategoria(outroToken, "Mercado", "DESPESA");

        criar(token, corpo("Feira", "25.50", categoriaDoOutro, "2026-10-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void naoDeveBuscarLancamentoDeOutroUsuario() throws Exception {
        String id = idDe(criar(token, corpo("Feira", "25.50", categoriaDespesa, "2026-10-31")));
        String outroToken = registrarEAutenticar("joao@financeflow.dev");

        mockMvc.perform(get("/api/lancamentos/" + id).header(HttpHeaders.AUTHORIZATION, bearer(outroToken)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarLancamento() throws Exception {
        String id = idDe(criar(token, corpo("Feira", "25.50", categoriaDespesa, "2026-10-31")));

        atualizar(token, id, """
                {"descricao": "Feira do mês", "valor": 99.90, "categoriaId": "%s", "dataLancamento": "2026-10-31"}
                """.formatted(categoriaDespesa))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.descricao").value("Feira do mês"))
            .andExpect(jsonPath("$.valor").value(99.9));
    }

    @Test
    void naoDeveTrocarParaCategoriaDeOutroTipo() throws Exception {
        String id = idDe(criar(token, corpo("Feira", "25.50", categoriaDespesa, "2026-10-31")));

        atualizar(token, id, corpo("Feira", "25.50", categoriaReceita, "2026-10-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveManterNaturezaQuandoNaoInformadaNaAtualizacao() throws Exception {
        String id = idDe(criar(token, """
                {"descricao": "Internet", "valor": 120.00, "categoriaId": "%s",
                 "dataLancamento": "2026-10-05", "natureza": "FIXA"}
                """.formatted(categoriaDespesa)));

        atualizar(token, id, corpo("Internet", "129.90", categoriaDespesa, "2026-10-05"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.natureza").value("FIXA"))
            .andExpect(jsonPath("$.valor").value(129.9));
    }

    @Test
    void deveMarcarEDesfazerPagamento() throws Exception {
        String id = idDe(criar(token, corpo("Feira", "25.50", categoriaDespesa, "2026-10-31")));

        mockMvc.perform(post("/api/lancamentos/" + id + "/pagamento")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paga").value(true))
            .andExpect(jsonPath("$.dataPagamento").isNotEmpty());

        mockMvc.perform(post("/api/lancamentos/" + id + "/pagamento")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/lancamentos/" + id + "/pagamento")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paga").value(false))
            .andExpect(jsonPath("$.dataPagamento").doesNotExist());

        mockMvc.perform(delete("/api/lancamentos/" + id + "/pagamento")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void devePagarComDataInformada() throws Exception {
        String id = idDe(criar(token, corpo("Feira", "25.50", categoriaDespesa, "2026-10-31")));

        mockMvc.perform(post("/api/lancamentos/" + id + "/pagamento")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"dataPagamento": "2026-10-30"}
                                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dataPagamento").value("2026-10-30"));
    }

    @Test
    void deveExcluirLancamento() throws Exception {
        String id = idDe(criar(token, corpo("Feira", "25.50", categoriaDespesa, "2026-10-31")));

        mockMvc.perform(delete("/api/lancamentos/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/lancamentos/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isNotFound());
    }

    @Test
    void naoDeveExcluirCategoriaComLancamentos() throws Exception {
        criar(token, corpo("Feira", "25.50", categoriaDespesa, "2026-10-31"))
            .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/categorias/" + categoriaDespesa)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isConflict());
    }

    @Test
    void deveListarTodosOsLancamentosDoUsuario() throws Exception {
        popular();

        listar(token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos").value(3))
            .andExpect(jsonPath("$.conteudo[0].descricao").value("Projeto"));
    }

    @Test
    void deveFiltrarPorTipo() throws Exception {
        popular();

        listar(token, "tipo", "DESPESA")
            .andExpect(jsonPath("$.totalElementos").value(2));
    }

    @Test
    void deveFiltrarPorNatureza() throws Exception {
        popular();
        criar(token, """
                {"descricao": "Aluguel", "valor": 1500.00, "categoriaId": "%s",
                 "dataLancamento": "2026-10-05", "natureza": "FIXA"}
                """.formatted(categoriaDespesa)).andExpect(status().isCreated());

        listar(token, "natureza", "FIXA")
            .andExpect(jsonPath("$.totalElementos").value(1))
            .andExpect(jsonPath("$.conteudo[0].descricao").value("Aluguel"));
    }

    @Test
    void deveFiltrarPorPeriodo() throws Exception {
        popular();

        listar(token, "inicio", "2026-10-10", "fim", "2026-10-20")
            .andExpect(jsonPath("$.totalElementos").value(2));
    }

    @Test
    void deveFiltrarPorCategoria() throws Exception {
        popular();

        listar(token, "categoriaId", categoriaReceita)
            .andExpect(jsonPath("$.totalElementos").value(1))
            .andExpect(jsonPath("$.conteudo[0].descricao").value("Projeto"));
    }

    @Test
    void deveFiltrarPorStatusDePagamento() throws Exception {
        popular();

        listar(token, "paga", "true")
            .andExpect(jsonPath("$.totalElementos").value(1))
            .andExpect(jsonPath("$.conteudo[0].descricao").value("Restaurante"));
    }

    @Test
    void deveFiltrarApenasVencidos() throws Exception {
        popular();
        criar(token, """
                {"descricao": "Boleto antigo", "valor": 50.00, "categoriaId": "%s",
                 "dataLancamento": "2020-01-01", "dataVencimento": "2020-01-10"}
                """.formatted(categoriaDespesa)).andExpect(status().isCreated());

        listar(token, "vencidos", "true")
            .andExpect(jsonPath("$.totalElementos").value(1))
            .andExpect(jsonPath("$.conteudo[0].descricao").value("Boleto antigo"))
            .andExpect(jsonPath("$.conteudo[0].vencido").value(true));
    }

    @Test
    void deveBuscarPorTextoIgnorandoCaixa() throws Exception {
        popular();

        listar(token, "busca", "REST")
            .andExpect(jsonPath("$.totalElementos").value(1))
            .andExpect(jsonPath("$.conteudo[0].descricao").value("Restaurante"));
    }

    @Test
    void deveTratarCoringasDaBuscaComoTextoLiteral() throws Exception {
        popular();

        listar(token, "busca", "%")
            .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void devePaginarEOrdenar() throws Exception {
        popular();

        listar(token, "size", "2", "page", "0", "sort", "valor,desc")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.conteudo.length()").value(2))
            .andExpect(jsonPath("$.conteudo[0].valor").value(100.0))
            .andExpect(jsonPath("$.totalElementos").value(3))
            .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    void deveLimitarOTamanhoDaPagina() throws Exception {
        listar(token, "size", "1000")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tamanho").value(100));
    }

    @Test
    void deveRejeitarOrdenacaoPorCampoNaoPermitido() throws Exception {
        listar(token, "sort", "usuario.senhaHash,asc")
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarPeriodoInvertido() throws Exception {
        listar(token, "inicio", "2026-10-20", "fim", "2026-10-10")
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveIsolarAListagemEntreUsuarios() throws Exception {
        popular();
        String outroToken = registrarEAutenticar("joao@financeflow.dev");

        listar(outroToken)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void naoDeveListarSemToken() throws Exception {
        mockMvc.perform(get("/api/lancamentos"))
            .andExpect(status().isUnauthorized());
    }

    private void popular() throws Exception {
        criar(token, corpo("Feira", "10.00", categoriaDespesa, "2026-10-01"))
            .andExpect(status().isCreated());
        criar(token, """
                {"descricao": "Restaurante", "valor": 30.00, "categoriaId": "%s",
                 "dataLancamento": "2026-10-15", "dataPagamento": "2026-10-15"}
                """.formatted(categoriaDespesa))
            .andExpect(status().isCreated());
        criar(token, corpo("Projeto", "100.00", categoriaReceita, "2026-10-20"))
            .andExpect(status().isCreated());
    }

    private ResultActions criar(String tokenDoUsuario, String corpo) throws Exception {
        return mockMvc.perform(post("/api/lancamentos")
            .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario))
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo));
    }

    private ResultActions atualizar(String tokenDoUsuario, String id, String corpo) throws Exception {
        return mockMvc.perform(put("/api/lancamentos/" + id)
            .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario))
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo));
    }

    private ResultActions listar(String tokenDoUsuario, String... parametros) throws Exception {
        MockHttpServletRequestBuilder requisicao = get("/api/lancamentos")
            .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario));
        for (int i = 0; i < parametros.length; i += 2) {
            requisicao.param(parametros[i], parametros[i + 1]);
        }
        return mockMvc.perform(requisicao);
    }

    private String idDe(ResultActions resultado) throws Exception {
        String resposta = resultado
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private static String corpo(String descricao, String valor, String categoriaId, String dataLancamento) {
        return """
                {"descricao": "%s", "valor": %s, "categoriaId": "%s", "dataLancamento": "%s"}
                """.formatted(descricao, valor, categoriaId, dataLancamento);
    }
}
