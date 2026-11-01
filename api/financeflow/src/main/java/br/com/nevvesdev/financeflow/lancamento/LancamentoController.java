package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.compartilhado.web.PaginaResposta;
import br.com.nevvesdev.financeflow.seguranca.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoController {

    private final LancamentoService lancamentoService;

    public LancamentoController(LancamentoService lancamentoService) {
        this.lancamentoService = lancamentoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoResposta criar(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody NovoLancamentoRequisicao requisicao) {
        return lancamentoService.criar(UsuarioAutenticado.id(jwt), requisicao);
    }

    @GetMapping
    public PaginaResposta<LancamentoResposta> listar(
        @AuthenticationPrincipal Jwt jwt,
        FiltroLancamentos filtro,
        Pageable paginacao) {
        return lancamentoService.listar(UsuarioAutenticado.id(jwt), filtro, paginacao);
    }

    @GetMapping("/{id}")
    public LancamentoResposta buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return lancamentoService.buscar(UsuarioAutenticado.id(jwt), id);
    }

    @PutMapping("/{id}")
    public LancamentoResposta atualizar(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody AlteracaoLancamentoRequisicao requisicao) {
        return lancamentoService.atualizar(UsuarioAutenticado.id(jwt), id, requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        lancamentoService.excluir(UsuarioAutenticado.id(jwt), id);
    }

    @PostMapping("/{id}/pagamento")
    public LancamentoResposta pagar(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @RequestBody(required = false) PagamentoRequisicao requisicao) {
        return lancamentoService.marcarComoPaga(
            UsuarioAutenticado.id(jwt), id, requisicao != null ? requisicao.dataPagamento() : null);
    }

    @DeleteMapping("/{id}/pagamento")
    public LancamentoResposta desfazerPagamento(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return lancamentoService.desfazerPagamento(UsuarioAutenticado.id(jwt), id);
    }
}
