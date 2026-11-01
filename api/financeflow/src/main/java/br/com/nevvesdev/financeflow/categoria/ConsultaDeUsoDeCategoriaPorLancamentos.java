package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.categoria.ConsultaDeUsoDeCategoria;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConsultaDeUsoDeCategoriaPorLancamentos implements ConsultaDeUsoDeCategoria {

    private final LancamentoRepository lancamentoRepository;

    public ConsultaDeUsoDeCategoriaPorLancamentos(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }

    @Override
    public boolean estaEmUso(UUID categoriaId) {
        return lancamentoRepository.existsByCategoriaId(categoriaId);
    }
}
