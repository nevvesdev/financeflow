package br.com.nevvesdev.financeflow.seguranca;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "financeflow.cors")
public record CorsProperties(@NotEmpty List<String> origensPermitidas) {
}
