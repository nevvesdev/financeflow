package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.BaseApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoriaControllerTest extends BaseApiTest {

    private String token;

    @BeforeEach
    void autenticar() throws Exception {
        token = registrarEAutenticar("maria@financeflow.dev");
    }

    @Test
    void deveCriarCategoriasPadraoNoCadastro() throws Exception {
        mockMvc.perform(get("/api/categorias").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.nome == 'Moradia' && @.tipo == 'DESPESA')]").isNotEmpty())
            .andExpect(jsonPath("$[?(@.tipo == 'RECEITA')]").isNotEmpty());
    }

    @Test
    void deveCriarCategoria() throws Exception {
        criar(token, "Animais", "DESPESA")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.nome").value("Animais"))
            .andExpect(jsonPath("$.tipo").value("DESPESA"));
    }

    @Test
    void deveRejeitarNomeDuplicadoIgnorandoCaixa() throws Exception {
        criar(token, "MORADIA", "DESPESA")
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void devePermitirMesmoNomeEmTipoDiferente() throws Exception {
        criar(token, "Moradia", "RECEITA")
            .andExpect(status().isCreated());
    }

    @Test
    void deveRejeitarNomeEmBranco() throws Exception {
        criar(token, "", "DESPESA")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erros.nome").exists());
    }

    @Test
    void deveRejeitarTipoInvalido() throws Exception {
        criar(token, "Animais", "OUTRO")
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveFiltrarPorTipo() throws Exception {
        mockMvc.perform(get("/api/categorias")
                .param("tipo", "RECEITA")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.tipo == 'DESPESA')]").isEmpty())
            .andExpect(jsonPath("$[?(@.tipo == 'RECEITA')]").isNotEmpty());
    }

    @Test
    void deveRenomearCategoria() throws Exception {
        String id = criarCategoria(token, "Animais", "DESPESA");

        renomear(token, id, "Pets")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Pets"));
    }

    @Test
    void naoDeveRenomearParaNomeExistente() throws Exception {
        String id = criarCategoria(token, "Animais", "DESPESA");

        renomear(token, id, "Lazer")
            .andExpect(status().isConflict());
    }

    @Test
    void deveExcluirCategoriaSemLancamentos() throws Exception {
        String id = criarCategoria(token, "Animais", "DESPESA");

        mockMvc.perform(delete("/api/categorias/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categorias").header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == '" + id + "')]").isEmpty());
    }

    @Test
    void naoDeveAcessarCategoriaDeOutroUsuario() throws Exception {
        String id = criarCategoria(token, "Animais", "DESPESA");
        String outroToken = registrarEAutenticar("joao@financeflow.dev");

        renomear(outroToken, id, "Pets")
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/categorias/" + id).header(HttpHeaders.AUTHORIZATION, bearer(outroToken)))
            .andExpect(status().isNotFound());
    }

    @Test
    void naoDeveListarSemToken() throws Exception {
        mockMvc.perform(get("/api/categorias"))
            .andExpect(status().isUnauthorized());
    }

    private ResultActions criar(String tokenDoUsuario, String nome, String tipo) throws Exception {
        return mockMvc.perform(post("/api/categorias")
            .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                        {"nome": "%s", "tipo": "%s"}
                        """.formatted(nome, tipo)));
    }

    private ResultActions renomear(String tokenDoUsuario, String id, String novoNome) throws Exception {
        return mockMvc.perform(put("/api/categorias/" + id)
            .header(HttpHeaders.AUTHORIZATION, bearer(tokenDoUsuario))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                        {"nome": "%s"}
                        """.formatted(novoNome)));
    }
}
