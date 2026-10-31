package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.compartilhado.ConflitoException;

public class CategoriaDuplicadaException extends ConflitoException {

    public CategoriaDuplicadaException() {
        super("Já existe uma categoria com este nome e tipo");
    }
}
