package br.com.nevvesdev.financeflow.notificacao;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TipoNotificacaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 1);

    @Test
    void deveClassificarComoVencidaQuandoOPrazoJaPassou() {
        assertThat(TipoNotificacao.para(HOJE.minusDays(1), HOJE)).isEqualTo(TipoNotificacao.VENCIDA);
    }

    @Test
    void deveClassificarComoVenceHojeNoDiaDoVencimento() {
        assertThat(TipoNotificacao.para(HOJE, HOJE)).isEqualTo(TipoNotificacao.VENCE_HOJE);
    }

    @Test
    void deveClassificarComoVenceEmBreveQuandoOPrazoEstaNoFuturo() {
        assertThat(TipoNotificacao.para(HOJE.plusDays(3), HOJE)).isEqualTo(TipoNotificacao.VENCE_EM_BREVE);
    }
}
