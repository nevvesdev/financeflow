package br.com.nevvesdev.financeflow;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public abstract class BaseApiTest {

    private static final String SENHA_PADRAO = "senhaSegura123";

    @Autowired
    private WebApplicationContext contexto;

    protected MockMvc mockMvc;

    @BeforeEach
    protected void configurarMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
            .apply(springSecurity())
            .build();
    }

    protected String registrarEAutenticar(String email) throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"nome": "Usuario de Teste", "email": "%s", "senha": "%s"}
                                """.formatted(email, SENHA_PADRAO)))
            .andExpect(status().isCreated());

        String resposta = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"email": "%s", "senha": "%s"}
                                """.formatted(email, SENHA_PADRAO)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return JsonPath.read(resposta, "$.tokenAcesso");
    }

    protected String criarCategoria(String token, String nome, String tipo) throws Exception {
        String resposta = mockMvc.perform(post("/api/categorias")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"nome": "%s", "tipo": "%s"}
                                """.formatted(nome, tipo)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return JsonPath.read(resposta, "$.id");
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }
}
