package br.com.nevvesdev.financeflow.seguranca;

import java.time.Instant;

public record TokenAcesso(String valor, Instant expiraEm) {
}
