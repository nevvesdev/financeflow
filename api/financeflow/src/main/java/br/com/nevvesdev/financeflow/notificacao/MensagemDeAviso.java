package br.com.nevvesdev.financeflow.notificacao;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class MensagemDeAviso {

    private static final Locale BRASIL = Locale.of("pt", "BR");

    private MensagemDeAviso() {
    }

    public static String montar(
        TipoNotificacao tipo,
        String descricao,
        BigDecimal valor,
        LocalDate vencimento,
        LocalDate hoje) {
        String dinheiro = NumberFormat.getCurrencyInstance(BRASIL).format(valor);
        long dias = Math.abs(ChronoUnit.DAYS.between(hoje, vencimento));

        return switch (tipo) {
            case VENCE_HOJE -> "%s vence hoje (%s)".formatted(descricao, dinheiro);
            case VENCE_EM_BREVE -> dias == 1
                ? "%s vence amanhã (%s)".formatted(descricao, dinheiro)
                : "%s vence em %d dias (%s)".formatted(descricao, dias, dinheiro);
            case VENCIDA -> dias == 1
                ? "%s está atrasada há 1 dia (%s)".formatted(descricao, dinheiro)
                : "%s está atrasada há %d dias (%s)".formatted(descricao, dias, dinheiro);
        };
    }
}
