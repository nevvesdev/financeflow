package br.com.nevvesdev.financeflow.categoria;

import br.com.nevvesdev.financeflow.compartilhado.TipoLancamento;
import br.com.nevvesdev.financeflow.usuario.Usuario;
import br.com.nevvesdev.financeflow.usuario.UsuarioCadastradoEvento;
import br.com.nevvesdev.financeflow.usuario.UsuarioRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CriadorDeCategoriasPadrao {

    private static final List<String> DESPESAS = List.of(
        "Moradia", "Alimentação", "Transporte", "Saúde",
        "Educação", "Lazer", "Assinaturas", "Outras despesas");

    private static final List<String> RECEITAS = List.of(
        "Salário", "Rendimentos", "Outras receitas");

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public CriadorDeCategoriasPadrao(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @EventListener
    public void aoCadastrarUsuario(UsuarioCadastradoEvento evento) {
        Usuario usuario = usuarioRepository.getReferenceById(evento.usuarioId());
        List<Categoria> categorias = new ArrayList<>();
        DESPESAS.forEach(nome -> categorias.add(Categoria.criar(usuario, nome, TipoLancamento.DESPESA)));
        RECEITAS.forEach(nome -> categorias.add(Categoria.criar(usuario, nome, TipoLancamento.RECEITA)));
        categoriaRepository.saveAll(categorias);
    }
}
