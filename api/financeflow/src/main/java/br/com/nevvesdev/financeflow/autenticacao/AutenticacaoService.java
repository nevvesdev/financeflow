package br.com.nevvesdev.financeflow.autenticacao;

import br.com.nevvesdev.financeflow.seguranca.EmissorDeToken;
import br.com.nevvesdev.financeflow.seguranca.TokenAcesso;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import br.com.nevvesdev.financeflow.usuario.UsuarioCadastradoEvento;
import br.com.nevvesdev.financeflow.usuario.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmissorDeToken emissorDeToken;
    private final ApplicationEventPublisher eventos;
    private final String hashFicticio;

    public AutenticacaoService(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        EmissorDeToken emissorDeToken,
        ApplicationEventPublisher eventos) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emissorDeToken = emissorDeToken;
        this.eventos = eventos;
        this.hashFicticio = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public Usuario cadastrar(String nome, String email, String senha) {
        String emailNormalizado = normalizar(email);
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException();
        }
        Usuario usuario = Usuario.criar(nome, emailNormalizado, passwordEncoder.encode(senha));
        Usuario salvo;
        try {
            salvo = usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException excecao) {
            throw new EmailJaCadastradoException();
        }
        eventos.publishEvent(new UsuarioCadastradoEvento(salvo.getId()));
        return salvo;
    }

    @Transactional(readOnly = true)
    public TokenAcesso autenticar(String email, String senha) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(normalizar(email));
        String hash = usuario.map(Usuario::getSenhaHash).orElse(hashFicticio);
        boolean senhaConfere = passwordEncoder.matches(senha, hash);
        return usuario
            .filter(encontrado -> senhaConfere)
            .map(emissorDeToken::emitir)
            .orElseThrow(CredenciaisInvalidasException::new);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPerfil(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId).orElseThrow(CredenciaisInvalidasException::new);
    }

    private static String normalizar(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
