package br.com.nevvesdev.financeflow.notificacao;

import java.time.LocalDate;

public enum TipoNotificacao {
    VENCE_EM_BREVE,
    VENCE_HOJE,
    VENCIDA;

    public static TipoNotificacao para(LocalDate vencimento, LocalDate hoje) {
        if (vencimento.isBefore(hoje)) {
            return VENCIDA;
        }
        if (vencimento.isEqual(hoje)) {
            return VENCE_HOJE;
        }
        return VENCE_EM_BREVE;
    }
}
