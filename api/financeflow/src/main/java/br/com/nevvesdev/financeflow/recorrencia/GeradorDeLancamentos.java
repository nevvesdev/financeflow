package br.com.nevvesdev.financeflow.recorrencia;

import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;
import br.com.nevvesdev.financeflow.lancamento.Lancamento;
import br.com.nevvesdev.financeflow.lancamento.LancamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class GeradorDeLancamentos {

    private static final int MESES_A_FRENTE = 1;

    private final RecorrenciaRepository recorrenciaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final Clock relogio;

    public GeradorDeLancamentos(
        RecorrenciaRepository recorrenciaRepository,
        LancamentoRepository lancamentoRepository,
        Clock relogio) {
        this.recorrenciaRepository = recorrenciaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.relogio = relogio;
    }

    @Transactional
    public int gerar(UUID recorrenciaId) {
        return recorrenciaRepository.findById(recorrenciaId)
            .map(this::gerarPendentes)
            .orElse(0);
    }

    private int gerarPendentes(Recorrencia recorrencia) {
        YearMonth mesAtual = YearMonth.now(relogio);
        List<YearMonth> pendentes = recorrencia.competenciasPendentes(mesAtual, mesAtual.plusMonths(MESES_A_FRENTE));

        for (YearMonth competencia : pendentes) {
            LocalDate vencimento = recorrencia.vencimentoEm(competencia);
            lancamentoRepository.save(Lancamento.criar(
                recorrencia.getUsuario(),
                recorrencia.getCategoria(),
                recorrencia.getDescricao(),
                recorrencia.getValorEstimado(),
                vencimento,
                vencimento,
                NaturezaLancamento.FIXA));
            recorrencia.registrarGeracao(competencia);
        }

        if (!pendentes.isEmpty()) {
            recorrenciaRepository.flush();
        }
        return pendentes.size();
    }
}
