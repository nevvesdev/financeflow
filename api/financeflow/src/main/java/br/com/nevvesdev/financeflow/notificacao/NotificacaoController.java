package br.com.nevvesdev.financeflow.notificacao;

import br.com.nevvesdev.financeflow.compartilhado.web.PaginaResposta;
import br.com.nevvesdev.financeflow.seguranca.UsuarioAutenticado;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public PaginaResposta<NotificacaoResposta> listar(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "false") boolean naoLidas,
        Pageable paginacao) {
        return notificacaoService.listar(UsuarioAutenticado.id(jwt), naoLidas, paginacao);
    }

    @GetMapping("/nao-lidas/total")
    public ContagemResposta contarNaoLidas(@AuthenticationPrincipal Jwt jwt) {
        return notificacaoService.contarNaoLidas(UsuarioAutenticado.id(jwt));
    }

    @PostMapping("/{id}/leitura")
    public NotificacaoResposta marcarComoLida(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return notificacaoService.marcarComoLida(UsuarioAutenticado.id(jwt), id);
    }

    @PostMapping("/leitura")
    public MarcacaoEmLoteResposta marcarTodasComoLidas(@AuthenticationPrincipal Jwt jwt) {
        return notificacaoService.marcarTodasComoLidas(UsuarioAutenticado.id(jwt));
    }
}
