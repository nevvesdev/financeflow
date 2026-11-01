package br.com.nevvesdev.financeflow.notificacao;

import br.com.nevvesdev.financeflow.compartilhado.Validacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notificacoes")
public class Notificacao {

    private static final int TAMANHO_MAXIMO_MENSAGEM = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "lancamento_id", nullable = false, updatable = false)
    private UUID lancamentoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private TipoNotificacao tipo;

    @Column(nullable = false, length = TAMANHO_MAXIMO_MENSAGEM, updatable = false)
    private String mensagem;

    @CreationTimestamp
    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(name = "enviada_em")
    private Instant enviadaEm;

    @Column(name = "lida_em")
    private Instant lidaEm;

    protected Notificacao() {
    }

    public static Notificacao criar(UUID usuarioId, UUID lancamentoId, TipoNotificacao tipo, String mensagem) {
        Notificacao notificacao = new Notificacao();
        notificacao.usuarioId = Validacao.obrigatorio(usuarioId, "usuário");
        notificacao.lancamentoId = Validacao.obrigatorio(lancamentoId, "lançamento");
        notificacao.tipo = Validacao.obrigatorio(tipo, "tipo");
        notificacao.mensagem = Validacao.textoObrigatorio(mensagem, "mensagem", TAMANHO_MAXIMO_MENSAGEM);
        return notificacao;
    }

    public void marcarComoLida(Instant agora) {
        if (lidaEm == null) {
            this.lidaEm = Validacao.obrigatorio(agora, "instante");
        }
    }

    public void marcarComoEnviada(Instant agora) {
        if (enviadaEm == null) {
            this.enviadaEm = Validacao.obrigatorio(agora, "instante");
        }
    }

    public boolean isLida() {
        return lidaEm != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public UUID getLancamentoId() {
        return lancamentoId;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getEnviadaEm() {
        return enviadaEm;
    }

    public Instant getLidaEm() {
        return lidaEm;
    }
}
