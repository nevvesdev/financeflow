package br.com.nevvesdev.financeflow.categoria;

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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categorias")
public class Categoria {

    private static final int TAMANHO_MAXIMO_NOME = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, updatable = false)
    private Usuario usuario;

    @Column(nullable = false, length = TAMANHO_MAXIMO_NOME)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private TipoLancamento tipo;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Categoria() {
    }

    private Categoria(Usuario usuario, String nome, TipoLancamento tipo) {
        this.usuario = Validacao.obrigatorio(usuario, "usuário");
        this.nome = Validacao.textoObrigatorio(nome, "nome", TAMANHO_MAXIMO_NOME);
        this.tipo = Validacao.obrigatorio(tipo, "tipo");
    }

    public static Categoria criar(Usuario usuario, String nome, TipoLancamento tipo) {
        return new Categoria(usuario, nome, tipo);
    }

    public void renomear(String novoNome) {
        this.nome = Validacao.textoObrigatorio(novoNome, "nome", TAMANHO_MAXIMO_NOME);
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNome() {
        return nome;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
