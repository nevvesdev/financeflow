package br.com.nevvesdev.financeflow.seguranca;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class UsuarioAutenticado {

    private UsuarioAutenticado() {
    }

    public static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
