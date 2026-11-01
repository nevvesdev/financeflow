package br.com.nevvesdev.financeflow.recorrencia;

import br.com.nevvesdev.financeflow.categoria.ConsultaDeUsoDeCategoria;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConsultaDeUsoDeCategoriaPorRecorrencias implements ConsultaDeUsoDeCategoria {

    private final RecorrenciaRepository recorrenciaRepository;

    public ConsultaDeUsoDeCategoriaPorRecorrencias(RecorrenciaRepository recorrenciaRepository) {
        this.recorrenciaRepository = recorrenciaRepository;
    }

    @Override
    public boolean estaEmUso(UUID categoriaId) {
        return recorrenciaRepository.existsByCategoriaId(categoriaId);
    }
}
