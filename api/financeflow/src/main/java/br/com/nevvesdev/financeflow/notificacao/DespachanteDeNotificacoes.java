package br.com.nevvesdev.financeflow.notificacao;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
public class DespachanteDeNotificacoes {

    private static final int TAMANHO_DO_LOTE = 200;

    private final NotificacaoRepository notificacaoRepository;
    private final List<CanalDeNotificacao> canais;
    private final Clock relogio;

    public DespachanteDeNotificacoes(
        NotificacaoRepository notificacaoRepository,
        List<CanalDeNotificacao> canais,
        Clock relogio) {
        this.notificacaoRepository = notificacaoRepository;
        this.canais = canais;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public List<UUID> buscarPendentes() {
        return notificacaoRepository.buscarIdsPendentesDeEnvio(PageRequest.of(0, TAMANHO_DO_LOTE));
    }

    @Transactional
    public boolean despachar(UUID notificacaoId) {
        return notificacaoRepository.findById(notificacaoId)
            .filter(notificacao -> notificacao.getEnviadaEm() == null)
            .map(this::enviar)
            .orElse(false);
    }

    private boolean enviar(Notificacao notificacao) {
        AvisoParaEnvio aviso = new AvisoParaEnvio(
            notificacao.getId(),
            notificacao.getUsuarioId(),
            notificacao.getTipo(),
            notificacao.getMensagem());
        canais.forEach(canal -> canal.enviar(aviso));
        notificacao.marcarComoEnviada(relogio.instant());
        return true;
    }
}
