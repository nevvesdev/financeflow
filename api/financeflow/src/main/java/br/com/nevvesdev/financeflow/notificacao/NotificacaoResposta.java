package br.com.nevvesdev.financeflow.notificacao;

import java.time.Instant;
import java.util.UUID;

public record NotificacaoResposta(
    UUID id,
    UUID lancamentoId,
    TipoNotificacao tipo,
    String mensagem,
    Instant criadaEm,
    boolean lida,
    Instant lidaEm) {

    public static NotificacaoResposta de(Notificacao notificacao) {
        return new NotificacaoResposta(
            notificacao.getId(),
            notificacao.getLancamentoId(),
            notificacao.getTipo(),
            notificacao.getMensagem(),
            notificacao.getCriadaEm(),
            notificacao.isLida(),
            notificacao.getLidaEm());
    }
}
