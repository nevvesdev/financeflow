package br.com.nevvesdev.financeflow.compartilhado;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "financeflow.agendamento.habilitado", havingValue = "true", matchIfMissing = true)
public class AgendamentoConfig {
}
