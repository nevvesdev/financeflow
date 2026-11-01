package br.com.nevvesdev.financeflow.painel;

import br.com.nevvesdev.financeflow.compartilhado.NaturezaLancamento;
import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import br.com.nevvesdev.financeflow.lancamento.LancamentoRepository;
import br.com.nevvesdev.financeflow.lancamento.TotalDeContasAbertas;
import br.com.nevvesdev.financeflow.lancamento.TotalPorCategoria;
import br.com.nevvesdev.financeflow.lancamento.TotalPorNatureza;
import br.com.nevvesdev.financeflow.lancamento.TotalPorTipoEPagamento;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class PainelService {

    private static final int DIAS_A_FRENTE = 7;
    private static final int LIMITE_DE_CONTAS = 50;
    private static final int ESCALA_MONETARIA = 2;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final LancamentoRepository lancamentoRepository;
    private final Clock relogio;

    public PainelService(LancamentoRepository lancamentoRepository, Clock relogio) {
        this.lancamentoRepository = lancamentoRepository;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public PainelResposta montar(UUID usuarioId, YearMonth mes) {
        LocalDate hoje = LocalDate.now(relogio);
        YearMonth mesDoPainel = mes != null ? mes : YearMonth.from(hoje);
        LocalDate inicio = mesDoPainel.atDay(1);
        LocalDate fim = mesDoPainel.atEndOfMonth();

        List<TotalPorTipoEPagamento> totais =
            lancamentoRepository.totalizarPorTipoEPagamento(usuarioId, inicio, fim);
        List<TotalPorCategoria> categorias =
            lancamentoRepository.totalizarPorCategoria(usuarioId, TipoLancamento.DESPESA, inicio, fim);
        List<TotalPorNatureza> naturezas =
            lancamentoRepository.totalizarPorNatureza(usuarioId, TipoLancamento.DESPESA, inicio, fim);

        return new PainelResposta(
            mesDoPainel.toString(),
            inicio,
            fim,
            totaisDe(totais, false),
            totaisDe(totais, true),
            contasAVencer(usuarioId, hoje),
            gastosPorCategoria(categorias),
            despesasPorNatureza(naturezas));
    }

    private ContasAVencerResposta contasAVencer(UUID usuarioId, LocalDate hoje) {
        LocalDate limite = hoje.plusDays(DIAS_A_FRENTE);
        TotalDeContasAbertas total =
            lancamentoRepository.totalizarContasAbertasAte(usuarioId, TipoLancamento.DESPESA, limite);
        List<ContaAVencerResposta> itens = lancamentoRepository
            .buscarContasAbertasAte(
                usuarioId, TipoLancamento.DESPESA, limite, PageRequest.of(0, LIMITE_DE_CONTAS))
            .stream()
            .map(lancamento -> ContaAVencerResposta.de(lancamento, hoje))
            .toList();
        return new ContasAVencerResposta(limite, total.quantidade(), dinheiro(total.valor()), itens);
    }

    private static TotaisResposta totaisDe(List<TotalPorTipoEPagamento> linhas, boolean apenasPagas) {
        BigDecimal entradas = somar(linhas, TipoLancamento.RECEITA, apenasPagas);
        BigDecimal saidas = somar(linhas, TipoLancamento.DESPESA, apenasPagas);
        return new TotaisResposta(dinheiro(entradas), dinheiro(saidas), dinheiro(entradas.subtract(saidas)));
    }

    private static BigDecimal somar(List<TotalPorTipoEPagamento> linhas, TipoLancamento tipo, boolean apenasPagas) {
        return linhas.stream()
            .filter(linha -> linha.tipo() == tipo)
            .filter(linha -> !apenasPagas || Boolean.TRUE.equals(linha.paga()))
            .map(TotalPorTipoEPagamento::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<GastoPorCategoriaResposta> gastosPorCategoria(List<TotalPorCategoria> linhas) {
        BigDecimal totalGeral = linhas.stream()
            .map(TotalPorCategoria::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return linhas.stream()
            .map(linha -> new GastoPorCategoriaResposta(
                linha.categoriaId(),
                linha.categoriaNome(),
                dinheiro(linha.total()),
                percentual(linha.total(), totalGeral)))
            .toList();
    }

    private static DespesasPorNaturezaResposta despesasPorNatureza(List<TotalPorNatureza> linhas) {
        return new DespesasPorNaturezaResposta(
            dinheiro(totalDaNatureza(linhas, NaturezaLancamento.FIXA)),
            dinheiro(totalDaNatureza(linhas, NaturezaLancamento.VARIAVEL)));
    }

    private static BigDecimal totalDaNatureza(List<TotalPorNatureza> linhas, NaturezaLancamento natureza) {
        return linhas.stream()
            .filter(linha -> linha.natureza() == natureza)
            .map(TotalPorNatureza::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal percentual(BigDecimal parte, BigDecimal todo) {
        if (todo.signum() == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.UNNECESSARY);
        }
        return parte.multiply(CEM).divide(todo, 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        BigDecimal base = valor != null ? valor : BigDecimal.ZERO;
        return base.setScale(ESCALA_MONETARIA, RoundingMode.UNNECESSARY);
    }
}
