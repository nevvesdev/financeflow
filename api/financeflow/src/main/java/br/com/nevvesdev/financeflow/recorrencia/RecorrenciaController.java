package br.com.nevvesdev.financeflow.recorrencia;

import br.com.nevvesdev.financeflow.seguranca.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recorrencias")
public class RecorrenciaController {

    private final RecorrenciaService recorrenciaService;

    public RecorrenciaController(RecorrenciaService recorrenciaService) {
        this.recorrenciaService = recorrenciaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecorrenciaResposta criar(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody NovaRecorrenciaRequisicao requisicao) {
        return recorrenciaService.criar(UsuarioAutenticado.id(jwt), requisicao);
    }

    @GetMapping
    public List<RecorrenciaResposta> listar(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) Boolean ativa) {
        return recorrenciaService.listar(UsuarioAutenticado.id(jwt), ativa);
    }

    @GetMapping("/{id}")
    public RecorrenciaResposta buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return recorrenciaService.buscar(UsuarioAutenticado.id(jwt), id);
    }

    @PutMapping("/{id}")
    public RecorrenciaResposta atualizar(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody AlteracaoRecorrenciaRequisicao requisicao) {
        return recorrenciaService.atualizar(UsuarioAutenticado.id(jwt), id, requisicao);
    }

    @PostMapping("/{id}/pausar")
    public RecorrenciaResposta pausar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return recorrenciaService.pausar(UsuarioAutenticado.id(jwt), id);
    }

    @PostMapping("/{id}/retomar")
    public RecorrenciaResposta retomar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return recorrenciaService.retomar(UsuarioAutenticado.id(jwt), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        recorrenciaService.excluir(UsuarioAutenticado.id(jwt), id);
    }
}
