package br.com.nevvesdev.financeflow.notificacao;

import br.com.nevvesdev.financeflow.BaseApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DespachoDeNotificacoesTest extends BaseApiTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @MockitoBean(name = "canalDeLog")
    private CanalDeNotificacao canal;

    @Autowired
    private AgendadorDeNotificacoes agendador;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Test
    void deveManterPendenteQuandoOCanalFalhaEReenviarNaProximaExecucao() throws Exception {
        String token = registrarEAutenticar("maria@financeflow.dev");
        String categoria = criarCategoria(token, "Aluguel", "DESPESA");
        LocalDate hoje = LocalDate.now(FUSO);
        mockMvc.perform(post("/api/lancamentos")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"descricao": "Aluguel", "valor": 1500.00, "categoriaId": "%s",
                                 "dataLancamento": "%s", "dataVencimento": "%s"}
                                """.formatted(categoria, hoje, hoje)))
            .andExpect(status().isCreated());

        doThrow(new IllegalStateException("canal indisponível")).doNothing().when(canal).enviar(any());

        agendador.executar();

        assertThat(notificacaoRepository.findAll())
            .hasSize(1)
            .allSatisfy(notificacao -> assertThat(notificacao.getEnviadaEm()).isNull());

        agendador.executar();

        assertThat(notificacaoRepository.findAll())
            .hasSize(1)
            .allSatisfy(notificacao -> assertThat(notificacao.getEnviadaEm()).isNotNull());
        verify(canal, times(2)).enviar(any());
    }
}
