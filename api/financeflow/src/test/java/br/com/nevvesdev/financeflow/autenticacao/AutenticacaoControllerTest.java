package br.com.nevvesdev.financeflow.autenticacao;

import br.com.nevvesdev.financeflow.BaseApiTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutenticacaoControllerTest extends BaseApiTest {

    private static final String EMAIL = "maria@financeflow.dev";
    private static final String SENHA = "senhaSegura123";

    @Test
    void deveCadastrarUsuarioSemExporSenha() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoCadastro("Maria Silva", EMAIL, SENHA)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.nome").value("Maria Silva"))
            .andExpect(jsonPath("$.email").value(EMAIL))
            .andExpect(jsonPath("$.senha").doesNotExist())
            .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveNormalizarEmailNoCadastro() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoCadastro("Maria Silva", "MARIA@FINANCEFLOW.DEV", SENHA)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void deveRejeitarEmailDuplicadoIgnorandoCaixa() throws Exception {
        cadastrar(EMAIL);

        mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoCadastro("Outra Maria", "MARIA@FINANCEFLOW.DEV", SENHA)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveRetornarErrosDeValidacaoPorCampo() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoCadastro("", "invalido", "123")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erros.nome").exists())
            .andExpect(jsonPath("$.erros.email").exists())
            .andExpect(jsonPath("$.erros.senha").exists());
    }

    @Test
    void deveRejeitarCorpoMalformado() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveAutenticarComCredenciaisValidas() throws Exception {
        cadastrar(EMAIL);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoLogin(EMAIL, SENHA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenAcesso").isNotEmpty())
            .andExpect(jsonPath("$.tipo").value("Bearer"))
            .andExpect(jsonPath("$.expiraEm").isNotEmpty());
    }

    @Test
    void naoDeveAutenticarComSenhaIncorreta() throws Exception {
        cadastrar(EMAIL);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoLogin(EMAIL, "senhaErrada123")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void naoDeveAutenticarEmailInexistente() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoLogin("ninguem@financeflow.dev", SENHA)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void naoDeveAcessarPerfilSemToken() throws Exception {
        mockMvc.perform(get("/api/auth/eu"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void deveAcessarPerfilComToken() throws Exception {
        cadastrar(EMAIL);
        String token = autenticar(EMAIL, SENHA);

        mockMvc.perform(get("/api/auth/eu")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(EMAIL))
            .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    void naoDeveAcessarPerfilComTokenAdulterado() throws Exception {
        cadastrar(EMAIL);
        String token = autenticar(EMAIL, SENHA);

        mockMvc.perform(get("/api/auth/eu")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token + "x"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirHealthCheckSemAutenticacao() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    private void cadastrar(String email) throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoCadastro("Maria Silva", email, SENHA)))
            .andExpect(status().isCreated());
    }

    private String autenticar(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoLogin(email, senha)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return JsonPath.read(resposta, "$.tokenAcesso");
    }

    private static String corpoCadastro(String nome, String email, String senha) {
        return """
                {"nome": "%s", "email": "%s", "senha": "%s"}
                """.formatted(nome, email, senha);
    }

    private static String corpoLogin(String email, String senha) {
        return """
                {"email": "%s", "senha": "%s"}
                """.formatted(email, senha);
    }
}
