package br.com.nevvesdev.financeflow.notificacao;

import java.util.UUID;

public record AvisoParaEnvio(UUID notificacaoId, UUID usuarioId, TipoNotificacao tipo, String mensagem) {
}
