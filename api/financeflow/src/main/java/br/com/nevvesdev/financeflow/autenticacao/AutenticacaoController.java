package br.com.nevvesdev.financeflow.autenticacao;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrar(@Valid @RequestBody CadastroRequisicao requisicao) {
        return UsuarioResposta.de(
            autenticacaoService.cadastrar(requisicao.nome(), requisicao.email(), requisicao.senha()));
    }

    @PostMapping("/login")
    public TokenResposta entrar(@Valid @RequestBody LoginRequisicao requisicao) {
        return TokenResposta.de(autenticacaoService.autenticar(requisicao.email(), requisicao.senha()));
    }

    @GetMapping("/eu")
    public UsuarioResposta perfil(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioResposta.de(autenticacaoService.buscarPerfil(UUID.fromString(jwt.getSubject())));
    }
}
