package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.compartilhado.ConflitoException;

public class CategoriaEmUsoException extends ConflitoException {

    public CategoriaEmUsoException() {
        super("A categoria possui lançamentos e não pode ser excluída");
    }
}
