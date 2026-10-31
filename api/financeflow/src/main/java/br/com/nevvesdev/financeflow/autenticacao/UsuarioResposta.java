package br.com.nevvesdev.financeflow.autenticacao;

import br.com.nevvesdev.financeflow.usuario.Usuario;

import java.util.UUID;

public record UsuarioResposta(UUID id, String nome, String email) {

    public static UsuarioResposta de(Usuario usuario) {
        return new UsuarioResposta(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
