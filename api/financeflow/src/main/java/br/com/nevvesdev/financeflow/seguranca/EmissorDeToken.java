package br.com.nevvesdev.financeflow.seguranca;

import br.com.nevvesdev.financeflow.usuario.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class EmissorDeToken {

    private static final String CLAIM_EMAIL = "email";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties propriedades;
    private final Clock relogio;

    public EmissorDeToken(JwtEncoder jwtEncoder, JwtProperties propriedades, Clock relogio) {
        this.jwtEncoder = jwtEncoder;
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    public TokenAcesso emitir(Usuario usuario) {
        Instant emitidoEm = relogio.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expiraEm = emitidoEm.plus(propriedades.expiracao());

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(propriedades.emissor())
            .subject(usuario.getId().toString())
            .issuedAt(emitidoEm)
            .expiresAt(expiraEm)
            .claim(CLAIM_EMAIL, usuario.getEmail())
            .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();

        return new TokenAcesso(valor, expiraEm);
    }
}
