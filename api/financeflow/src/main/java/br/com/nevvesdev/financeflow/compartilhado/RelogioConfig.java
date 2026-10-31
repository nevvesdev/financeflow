package br.com.nevvesdev.financeflow.compartilhado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RelogioConfig {

    @Bean
    public Clock relogio(@Value("${financeflow.fuso-horario}") String fusoHorario) {
        return Clock.system(ZoneId.of(fusoHorario));
    }
}
