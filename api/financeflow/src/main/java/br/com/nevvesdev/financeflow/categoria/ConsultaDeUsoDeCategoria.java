package br.com.nevvesdev.financeflow.categoria;

import java.util.UUID;

public interface ConsultaDeUsoDeCategoria {

    boolean estaEmUso(UUID categoriaId);
}
