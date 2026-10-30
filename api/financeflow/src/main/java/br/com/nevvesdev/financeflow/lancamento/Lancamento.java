package br.com.nevvesdev.financeflow.lancamento;

import br.com.nevvesdev.financeflow.categoria.Categoria;
import br.com.nevvesdev.financeflow.compartilhado.RegraDeNegocioException;
import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import br.com.nevvesdev.financeflow.compartilhado.Validacao;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lancamentos")
public class Lancamento {

    private static final int TAMANHO_MAXIMO_DESCRICAO = 160;
    private static final int ESCALA_VALOR = 2;
    private static final int PRECISAO_VALOR = 19;

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

    @Column(nullable = false, precision = PRECISAO_VALOR, scale = ESCALA_VALOR)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private TipoLancamento tipo;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(nullable = false)
    private boolean paga;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Lancamento() {
    }

    public static Lancamento criar(
        Usuario usuario,
        Categoria categoria,
        String descricao,
        BigDecimal valor,
        LocalDate dataLancamento,
        LocalDate dataVencimento) {
        Lancamento lancamento = new Lancamento();
        lancamento.usuario = Validacao.obrigatorio(usuario, "usuário");
        lancamento.tipo = Validacao.obrigatorio(categoria, "categoria").getTipo();
        lancamento.preencher(categoria, descricao, valor, dataLancamento, dataVencimento);
        return lancamento;
    }

    public void atualizar(
        Categoria categoria,
        String descricao,
        BigDecimal valor,
        LocalDate dataLancamento,
        LocalDate dataVencimento) {
        if (Validacao.obrigatorio(categoria, "categoria").getTipo() != tipo) {
            throw new RegraDeNegocioException("categoria deve ser do mesmo tipo do lançamento");
        }
        preencher(categoria, descricao, valor, dataLancamento, dataVencimento);
    }

    public void marcarComoPaga(LocalDate dataPagamento) {
        if (paga) {
            throw new RegraDeNegocioException("lançamento já está pago");
        }
        this.dataPagamento = Validacao.obrigatorio(dataPagamento, "data de pagamento");
        this.paga = true;
    }

    public void desfazerPagamento() {
        if (!paga) {
            throw new RegraDeNegocioException("lançamento não está pago");
        }
        this.paga = false;
        this.dataPagamento = null;
    }

    public boolean estaVencido(LocalDate hoje) {
        return !paga && dataVencimento != null && dataVencimento.isBefore(hoje);
    }

    private void preencher(
        Categoria categoria,
        String descricao,
        BigDecimal valor,
        LocalDate dataLancamento,
        LocalDate dataVencimento) {
        this.categoria = categoria;
        this.descricao = Validacao.textoObrigatorio(descricao, "descrição", TAMANHO_MAXIMO_DESCRICAO);
        this.valor = normalizarValor(valor);
        this.dataLancamento = Validacao.obrigatorio(dataLancamento, "data do lançamento");
        this.dataVencimento = dataVencimento;
    }

    private static BigDecimal normalizarValor(BigDecimal valor) {
        Validacao.obrigatorio(valor, "valor");
        if (valor.signum() <= 0) {
            throw new RegraDeNegocioException("valor deve ser maior que zero");
        }
        if (valor.stripTrailingZeros().scale() > ESCALA_VALOR) {
            throw new RegraDeNegocioException("valor deve ter no máximo duas casas decimais");
        }
        BigDecimal normalizado = valor.setScale(ESCALA_VALOR, RoundingMode.UNNECESSARY);
        if (normalizado.precision() > PRECISAO_VALOR) {
            throw new RegraDeNegocioException("valor excede o limite permitido");
        }
        return normalizado;
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

    public BigDecimal getValor() {
        return valor;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public boolean isPaga() {
        return paga;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
