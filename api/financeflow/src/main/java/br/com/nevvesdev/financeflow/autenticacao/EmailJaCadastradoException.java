package br.com.nevvesdev.financeflow.autenticacao;

import br.com.nevvesdev.financeflow.compartilhado.ConflitoException;

public class EmailJaCadastradoException extends ConflitoException {

    public EmailJaCadastradoException() {
        super("Já existe uma conta com este e-mail");
    }
}
