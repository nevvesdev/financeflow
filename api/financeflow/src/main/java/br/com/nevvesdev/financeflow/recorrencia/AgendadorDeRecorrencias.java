package br.com.nevvesdev.financeflow.recorrencia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AgendadorDeRecorrencias {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorDeRecorrencias.class);

    private final RecorrenciaRepository recorrenciaRepository;
    private final GeradorDeLancamentos gerador;

    public AgendadorDeRecorrencias(RecorrenciaRepository recorrenciaRepository, GeradorDeLancamentos gerador) {
        this.recorrenciaRepository = recorrenciaRepository;
        this.gerador = gerador;
    }

    @Scheduled(cron = "${financeflow.recorrencias.cron}", zone = "${financeflow.fuso-horario}")
    public void gerarLancamentos() {
        List<UUID> ids = recorrenciaRepository.buscarIdsAtivas();
        int gerados = 0;
        for (UUID id : ids) {
            try {
                gerados += gerador.gerar(id);
            } catch (RuntimeException excecao) {
                LOGGER.error("Falha ao gerar lançamentos da recorrência {}", id, excecao);
            }
        }
        LOGGER.info("Geração de recorrências concluída: {} analisadas, {} lançamentos gerados", ids.size(), gerados);
    }
}
