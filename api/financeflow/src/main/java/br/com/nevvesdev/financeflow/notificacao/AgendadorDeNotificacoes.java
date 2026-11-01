package br.com.nevvesdev.financeflow.notificacao;

import br.com.nevvesdev.financeflow.lancamento.ContaParaAviso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AgendadorDeNotificacoes {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorDeNotificacoes.class);

    private final GeradorDeNotificacoes gerador;
    private final DespachanteDeNotificacoes despachante;

    public AgendadorDeNotificacoes(GeradorDeNotificacoes gerador, DespachanteDeNotificacoes despachante) {
        this.gerador = gerador;
        this.despachante = despachante;
    }

    @Scheduled(cron = "${financeflow.notificacoes.cron}", zone = "${financeflow.fuso-horario}")
    public void executar() {
        int geradas = gerarAvisos();
        int enviadas = despacharPendentes();
        LOGGER.info("Notificações concluídas: {} geradas, {} enviadas", geradas, enviadas);
    }

    private int gerarAvisos() {
        int geradas = 0;
        for (ContaParaAviso conta : gerador.buscarContas()) {
            try {
                if (gerador.gerarPara(conta)) {
                    geradas++;
                }
            } catch (RuntimeException excecao) {
                LOGGER.error("Falha ao gerar aviso do lançamento {}", conta.lancamentoId(), excecao);
            }
        }
        return geradas;
    }

    private int despacharPendentes() {
        int enviadas = 0;
        List<UUID> pendentes = despachante.buscarPendentes();
        for (UUID id : pendentes) {
            try {
                if (despachante.despachar(id)) {
                    enviadas++;
                }
            } catch (RuntimeException excecao) {
                LOGGER.error("Falha ao enviar a notificação {}", id, excecao);
            }
        }
        return enviadas;
    }
}
