package br.com.nevvesdev.financeflow.recorrencia;

import br.com.nevvesdev.financeflow.categoria.Categoria;
import br.com.nevvesdev.financeflow.compartilhado.RegraDeNegocioException;
import br.com.nevvesdev.financeflow.compartilhado.Validacao;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recorrencias")
public class Recorrencia {

    private static final int TAMANHO_MAXIMO_DESCRICAO = 160;
    private static final int PRIMEIRO_DIA = 1;
    private static final int ULTIMO_DIA = 31;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = TAMANHO_MAXIMO_DESCRICAO)
    private String descricao;

    @Column(name = "valor_estimado", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "dia_vencimento", nullable = false)
    private int diaVencimento;

    @Column(name = "data_inicio", nullable = false, updatable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(name = "ultima_competencia_gerada")
    private LocalDate ultimaCompetenciaGerada;

    @Version
    @Column(nullable = false)
    private Long versao;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Recorrencia() {
    }

    public static Recorrencia criar(
        Usuario usuario,
        Categoria categoria,
        String descricao,
        BigDecimal valorEstimado,
        int diaVencimento,
        LocalDate dataInicio,
        LocalDate dataFim) {
        Recorrencia recorrencia = new Recorrencia();
        recorrencia.usuario = Validacao.obrigatorio(usuario, "usuário");
        recorrencia.dataInicio = Validacao.obrigatorio(dataInicio, "data de início");
        recorrencia.preencher(categoria, descricao, valorEstimado, diaVencimento, dataFim);
        return recorrencia;
    }

    public void atualizar(
        Categoria categoria,
        String descricao,
        BigDecimal valorEstimado,
        int diaVencimento,
        LocalDate dataFim) {
        if (Validacao.obrigatorio(categoria, "categoria").getTipo() != this.categoria.getTipo()) {
            throw new RegraDeNegocioException("categoria deve ser do mesmo tipo da recorrência");
        }
        preencher(categoria, descricao, valorEstimado, diaVencimento, dataFim);
    }

    public void pausar() {
        if (!ativa) {
            throw new RegraDeNegocioException("recorrência já está pausada");
        }
        this.ativa = false;
    }

    public void retomar() {
        if (ativa) {
            throw new RegraDeNegocioException("recorrência já está ativa");
        }
        this.ativa = true;
    }

    public LocalDate vencimentoEm(YearMonth competencia) {
        return competencia.atDay(Math.min(diaVencimento, competencia.lengthOfMonth()));
    }

    public List<YearMonth> competenciasPendentes(YearMonth mesAtual, YearMonth ate) {
        if (!ativa) {
            return List.of();
        }
        YearMonth primeira = maior(YearMonth.from(dataInicio), mesAtual);
        if (ultimaCompetenciaGerada != null) {
            primeira = maior(primeira, YearMonth.from(ultimaCompetenciaGerada).plusMonths(1));
        }
        YearMonth ultima = dataFim != null ? menor(ate, YearMonth.from(dataFim)) : ate;

        List<YearMonth> pendentes = new ArrayList<>();
        for (YearMonth competencia = primeira; !competencia.isAfter(ultima); competencia = competencia.plusMonths(1)) {
            pendentes.add(competencia);
        }
        return pendentes;
    }

    public void registrarGeracao(YearMonth competencia) {
        this.ultimaCompetenciaGerada = Validacao.obrigatorio(competencia, "competência").atDay(1);
    }

    private void preencher(
        Categoria categoria,
        String descricao,
        BigDecimal valorEstimado,
        int diaVencimento,
        LocalDate dataFim) {
        if (diaVencimento < PRIMEIRO_DIA || diaVencimento > ULTIMO_DIA) {
            throw new RegraDeNegocioException("dia de vencimento deve estar entre 1 e 31");
        }
        if (dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new RegraDeNegocioException("data final não pode ser anterior à data inicial");
        }
        this.categoria = Validacao.obrigatorio(categoria, "categoria");
        this.descricao = Validacao.textoObrigatorio(descricao, "descrição", TAMANHO_MAXIMO_DESCRICAO);
        this.valorEstimado = Validacao.valorMonetario(valorEstimado);
        this.diaVencimento = diaVencimento;
        this.dataFim = dataFim;
    }

    private static YearMonth maior(YearMonth a, YearMonth b) {
        return a.isAfter(b) ? a : b;
    }

    private static YearMonth menor(YearMonth a, YearMonth b) {
        return a.isBefore(b) ? a : b;
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public int getDiaVencimento() {
        return diaVencimento;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public LocalDate getUltimaCompetenciaGerada() {
        return ultimaCompetenciaGerada;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
