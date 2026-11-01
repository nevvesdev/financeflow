package br.com.nevvesdev.financeflow.notificacao;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import br.com.nevvesdev.financeflow.lancamento.ContaParaAviso;
import br.com.nevvesdev.financeflow.lancamento.ContasParaAvisoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class GeradorDeNotificacoes {

    private static final int DIAS_DE_ANTECEDENCIA = 3;
    private static final int DIAS_MAXIMOS_DE_ATRASO = 30;

    private final ContasParaAvisoRepository contasRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final Clock relogio;

    public GeradorDeNotificacoes(
        ContasParaAvisoRepository contasRepository,
        NotificacaoRepository notificacaoRepository,
        Clock relogio) {
        this.contasRepository = contasRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public List<ContaParaAviso> buscarContas() {
        LocalDate hoje = LocalDate.now(relogio);
        return contasRepository.buscarAbertasComVencimentoEntre(
            TipoLancamento.DESPESA,
            hoje.minusDays(DIAS_MAXIMOS_DE_ATRASO),
            hoje.plusDays(DIAS_DE_ANTECEDENCIA));
    }

    @Transactional
    public boolean gerarPara(ContaParaAviso conta) {
        LocalDate hoje = LocalDate.now(relogio);
        TipoNotificacao tipo = TipoNotificacao.para(conta.dataVencimento(), hoje);
        if (notificacaoRepository.existsByLancamentoIdAndTipo(conta.lancamentoId(), tipo)) {
            return false;
        }
        String mensagem = MensagemDeAviso.montar(
            tipo, conta.descricao(), conta.valor(), conta.dataVencimento(), hoje);
        notificacaoRepository.saveAndFlush(
            Notificacao.criar(conta.usuarioId(), conta.lancamentoId(), tipo, mensagem));
        return true;
    }
}
