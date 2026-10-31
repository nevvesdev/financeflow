package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
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
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResposta> listar(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) TipoLancamento tipo) {
        return categoriaService.listar(UsuarioAutenticado.id(jwt), tipo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResposta criar(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody NovaCategoriaRequisicao requisicao) {
        return categoriaService.criar(UsuarioAutenticado.id(jwt), requisicao.nome(), requisicao.tipo());
    }

    @PutMapping("/{id}")
    public CategoriaResposta renomear(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody RenomearCategoriaRequisicao requisicao) {
        return categoriaService.renomear(UsuarioAutenticado.id(jwt), id, requisicao.nome());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        categoriaService.excluir(UsuarioAutenticado.id(jwt), id);
    }
}
