package br.com.nevvesdev.financeflow.notificacao;

import br.com.nevvesdev.financeflow.BaseApiTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificacaoControllerTest extends BaseApiTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Autowired
    private AgendadorDeNotificacoes agendador;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    private LocalDate hoje;
    private String token;
    private String categoriaAluguel;
    private String categoriaFreelance;

    @BeforeEach
    void preparar() throws Exception {
        hoje = LocalDate.now(FUSO);
        token = registrarEAutenticar("maria@financeflow.dev");
        categoriaAluguel = criarCategoria(token, "Aluguel", "DESPESA");
        categoriaFreelance = criarCategoria(token, "Freelance", "RECEITA");
    }

    @Test
    void deveGerarAvisosParaContasEmBreveDeHojeEAtrasadas() throws Exception {
        lancar(token, "Em breve", "100.00", categoriaAluguel, hoje.plusDays(2), null);
        lancar(token, "Vence hoje", "50.00", categoriaAluguel, hoje, null);
        lancar(token, "Atrasada", "75.00", categoriaAluguel, hoje.minusDays(5), null);
        lancar(token, "Distante", "10.00", categoriaAluguel, hoje.plusDays(10), null);
        lancar(token, "Muito antiga", "10.00", categoriaAluguel, hoje.minusDays(40), null);
        lancar(token, "Ja paga", "10.00", categoriaAluguel, hoje, hoje);
        lancar(token, "Receita", "10.00", categoriaFreelance, hoje, null);

        agendador.executar();

        listar(token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos").value(3))
            .andExpect(jsonPath("$.conteudo.length()").value(3))
            .andExpect(jsonPath("$.conteudo[?(@.tipo == 'VENCE_EM_BREVE')]").isNotEmpty())
            .andExpect(jsonPath("$.conteudo[?(@.tipo == 'VENCE_HOJE')]").isNotEmpty())
            .andExpect(jsonPath("$.conteudo[?(@.tipo == 'VENCIDA')]").isNotEmpty());
    }

    @Test
    void deveIncluirOLancamentoEATextoDoAviso() throws Exception {
        String lancamentoId = lancar(token, "Aluguel", "1500.00", categoriaAluguel, hoje, null);

        agendador.executar();

        String resposta = listar(token)
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String mensagem = JsonPath.read(resposta, "$.conteudo[0].mensagem");

        assertThat(mensagem).startsWith("Aluguel vence hoje");
        assertThat((String) JsonPath.read(resposta, "$.conteudo[0].lancamentoId")).isEqualTo(lancamentoId);
        assertThat((Boolean) JsonPath.read(resposta, "$.conteudo[0].lida")).isFalse();
    }

    @Test
    void naoDeveDuplicarAvisosAoExecutarOJobNovamente() throws Exception {
        lancar(token, "Vence hoje", "50.00", categoriaAluguel, hoje, null);

        agendador.executar();
        agendador.executar();

        listar(token)
            .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void deveMarcarOAvisoComoEnviadoDepoisDoDespacho() throws Exception {
        lancar(token, "Vence hoje", "50.00", categoriaAluguel, hoje, null);

        agendador.executar();

        assertThat(notificacaoRepository.findAll())
            .hasSize(1)
            .allSatisfy(notificacao -> assertThat(notificacao.getEnviadaEm()).isNotNull());
    }

    @Test
    void deveContarMarcarEListarApenasNaoLidas() throws Exception {
        lancar(token, "Vence hoje", "50.00", categoriaAluguel, hoje, null);
        lancar(token, "Atrasada", "75.00", categoriaAluguel, hoje.minusDays(2), null);
        agendador.executar();

        mockMvc.perform(get("/api/notificacoes/nao-lidas/total").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(2));

        String resposta = listar(token).andReturn().getResponse().getContentAsString();
        String primeiroId = JsonPath.read(resposta, "$.conteudo[0].id");

        mockMvc.perform(post("/api/notificacoes/" + primeiroId + "/leitura")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lida").value(true))
            .andExpect(jsonPath("$.lidaEm").isNotEmpty());

        mockMvc.perform(get("/api/notificacoes/nao-lidas/total").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(jsonPath("$.total").value(1));

        listar(token, "naoLidas", "true")
            .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void deveMarcarTodasComoLidas() throws Exception {
        lancar(token, "Vence hoje", "50.00", categoriaAluguel, hoje, null);
        lancar(token, "Atrasada", "75.00", categoriaAluguel, hoje.minusDays(2), null);
        agendador.executar();

        mockMvc.perform(post("/api/notificacoes/leitura").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.marcadas").value(2));

        listar(token, "naoLidas", "true")
            .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void naoDeveMarcarAvisoDeOutroUsuario() throws Exception {
        lancar(token, "Vence hoje", "50.00", categoriaAluguel, hoje, null);
        agendador.executar();
        String resposta = listar(token).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(resposta, "$.conteudo[0].id");
        String outroToken = registrarEAutenticar("joao@financeflow.dev");

        mockMvc.perform(post("/api/notificacoes/" + id + "/leitura")
                .header(HttpHeaders.AUTHORIZATION, bearer(outroToken)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deveIsolarAListagemEntreUsuarios() throws Exception {
        lancar(token, "Vence hoje", "50.00", categoriaAluguel, hoje, null);
        agendador.executar();
        String outroToken = registrarEAutenticar("joao@financeflow.dev");

        listar(outroToken)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void deveRemoverOsAvisosQuandoOLancamentoEExcluido() throws Exception {
        String lancamentoId = lancar(token, "Vence hoje", "50.00", categoriaAluguel, hoje, null);
        agendador.executar();

        mockMvc.perform(delete("/api/lancamentos/" + lancamentoId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isNoContent());

        listar(token)
            .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void deveLimitarOTamanhoDaPagina() throws Exception {
        listar(token, "size", "1000")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tamanho").value(100));
    }

    @Test
    void naoDeveListarSemToken() throws Exception {
        mockMvc.perform(get("/api/notificacoes"))
            .andExpect(status().isUnauthorized());
    }

    private ResultActions listar(String tokenDoUsuario, String... parametros) throws Exception {
        MockHttpServletRequestBuilder requisicao = get("/api/notificacoes")
            .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario));
        for (int i = 0; i < parametros.length; i += 2) {
            requisicao.param(parametros[i], parametros[i + 1]);
        }
        return mockMvc.perform(requisicao);
    }

    private String lancar(
        String tokenDoUsuario,
        String descricao,
        String valor,
        String categoriaId,
        LocalDate vencimento,
        LocalDate dataPagamento) throws Exception {
        StringBuilder corpo = new StringBuilder()
            .append("{\"descricao\": \"").append(descricao).append("\", ")
            .append("\"valor\": ").append(valor).append(", ")
            .append("\"categoriaId\": \"").append(categoriaId).append("\", ")
            .append("\"dataLancamento\": \"").append(hoje).append("\", ")
            .append("\"dataVencimento\": \"").append(vencimento).append("\"");
        if (dataPagamento != null) {
            corpo.append(", \"dataPagamento\": \"").append(dataPagamento).append("\"");
        }
        corpo.append("}");

        String resposta = mockMvc.perform(post("/api/lancamentos")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo.toString()))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }
}
