package br.com.nevvesdev.financeflow.autenticacao;

import br.com.nevvesdev.financeflow.seguranca.TokenAcesso;

import java.time.Instant;

public record TokenResposta(String tokenAcesso, String tipo, Instant expiraEm) {

    private static final String TIPO_BEARER = "Bearer";

    public static TokenResposta de(TokenAcesso token) {
        return new TokenResposta(token.valor(), TIPO_BEARER, token.expiraEm());
    }
}
