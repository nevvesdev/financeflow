package br.com.nevvesdev.financeflow.usuario;

import br.com.nevvesdev.financeflow.compartilhado.Validacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    private static final int TAMANHO_MAXIMO_NOME = 120;
    private static final int TAMANHO_MAXIMO_EMAIL = 160;
    private static final int TAMANHO_MAXIMO_SENHA_HASH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = TAMANHO_MAXIMO_NOME)
    private String nome;

    @Column(nullable = false, length = TAMANHO_MAXIMO_EMAIL, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = TAMANHO_MAXIMO_SENHA_HASH)
    private String senhaHash;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Usuario() {
    }

    private Usuario(String nome, String email, String senhaHash) {
        this.nome = Validacao.textoObrigatorio(nome, "nome", TAMANHO_MAXIMO_NOME);
        this.email = Validacao.textoObrigatorio(email, "email", TAMANHO_MAXIMO_EMAIL).toLowerCase(Locale.ROOT);
        this.senhaHash = Validacao.textoObrigatorio(senhaHash, "senha", TAMANHO_MAXIMO_SENHA_HASH);
    }

    public static Usuario criar(String nome, String email, String senhaHash) {
        return new Usuario(nome, email, senhaHash);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
