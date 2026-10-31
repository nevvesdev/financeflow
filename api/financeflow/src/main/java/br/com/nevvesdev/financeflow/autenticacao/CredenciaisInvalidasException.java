package br.com.nevvesdev.financeflow.autenticacao;

import br.com.nevvesdev.financeflow.compartilhado.NaoAutenticadoException;

public class CredenciaisInvalidasException extends NaoAutenticadoException {

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}
