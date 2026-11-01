package br.com.nevvesdev.financeflow.notificacao;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MensagemDeAvisoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 1);
    private static final BigDecimal VALOR = new BigDecimal("1500.00");

    @Test
    void deveAvisarQueVenceHoje() {
        String mensagem = MensagemDeAviso.montar(TipoNotificacao.VENCE_HOJE, "Aluguel", VALOR, HOJE, HOJE);

        assertThat(mensagem).startsWith("Aluguel vence hoje").contains("R$").contains("1.500,00");
    }

    @Test
    void deveAvisarQueVenceAmanha() {
        String mensagem = MensagemDeAviso.montar(
            TipoNotificacao.VENCE_EM_BREVE, "Aluguel", VALOR, HOJE.plusDays(1), HOJE);

        assertThat(mensagem).startsWith("Aluguel vence amanhã");
    }

    @Test
    void deveAvisarQuantosDiasFaltam() {
        String mensagem = MensagemDeAviso.montar(
            TipoNotificacao.VENCE_EM_BREVE, "Aluguel", VALOR, HOJE.plusDays(3), HOJE);

        assertThat(mensagem).startsWith("Aluguel vence em 3 dias");
    }

    @Test
    void deveAvisarAtrasoDeUmDiaNoSingular() {
        String mensagem = MensagemDeAviso.montar(
            TipoNotificacao.VENCIDA, "Aluguel", VALOR, HOJE.minusDays(1), HOJE);

        assertThat(mensagem).startsWith("Aluguel está atrasada há 1 dia ");
    }

    @Test
    void deveAvisarAtrasoDeVariosDiasNoPlural() {
        String mensagem = MensagemDeAviso.montar(
            TipoNotificacao.VENCIDA, "Aluguel", VALOR, HOJE.minusDays(5), HOJE);

        assertThat(mensagem).startsWith("Aluguel está atrasada há 5 dias");
    }
}
