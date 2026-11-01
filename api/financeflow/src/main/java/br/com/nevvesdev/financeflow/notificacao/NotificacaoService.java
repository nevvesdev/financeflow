package br.com.nevvesdev.financeflow.notificacao;

import br.com.nevvesdev.financeflow.compartilhado.RecursoNaoEncontradoException;
import br.com.nevvesdev.financeflow.compartilhado.web.PaginaResposta;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class NotificacaoService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final NotificacaoRepository notificacaoRepository;
    private final Clock relogio;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, Clock relogio) {
        this.notificacaoRepository = notificacaoRepository;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public PaginaResposta<NotificacaoResposta> listar(UUID usuarioId, boolean apenasNaoLidas, Pageable paginacao) {
        Pageable segura = PageRequest.of(
            paginacao.getPageNumber(), Math.min(paginacao.getPageSize(), TAMANHO_MAXIMO_PAGINA));
        return PaginaResposta.de(
            notificacaoRepository.listar(usuarioId, apenasNaoLidas, segura).map(NotificacaoResposta::de));
    }

    @Transactional(readOnly = true)
    public ContagemResposta contarNaoLidas(UUID usuarioId) {
        return new ContagemResposta(notificacaoRepository.countByUsuarioIdAndLidaEmIsNull(usuarioId));
    }

    @Transactional
    public NotificacaoResposta marcarComoLida(UUID usuarioId, UUID id) {
        Notificacao notificacao = notificacaoRepository.findByIdAndUsuarioId(id, usuarioId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada"));
        notificacao.marcarComoLida(relogio.instant());
        notificacaoRepository.flush();
        return NotificacaoResposta.de(notificacao);
    }

    @Transactional
    public MarcacaoEmLoteResposta marcarTodasComoLidas(UUID usuarioId) {
        return new MarcacaoEmLoteResposta(
            notificacaoRepository.marcarTodasComoLidas(usuarioId, relogio.instant()));
    }
}
