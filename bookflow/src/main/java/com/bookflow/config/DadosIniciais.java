package com.bookflow.config;

import com.bookflow.model.Livro;
import com.bookflow.model.Papel;
import com.bookflow.model.Usuario;
import com.bookflow.repository.LivroRepository;
import com.bookflow.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Stream;

/** Roda uma vez quando a aplicação sobe: cria as contas de teste e alguns livros. */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final PasswordEncoder passwordEncoder;

    public DadosIniciais(UsuarioRepository usuarioRepository, LivroRepository livroRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(new Usuario("Vendedor BookFlow", "vendedor@bookflow.com",
                    passwordEncoder.encode("vendedor123"), Papel.VENDEDOR));
            usuarioRepository.save(new Usuario("Cliente Teste", "cliente@bookflow.com",
                    passwordEncoder.encode("cliente123"), Papel.CLIENTE));
        }

        if (livroRepository.count() == 0) {
            Stream.of(
                    livro("Dom Casmurro", "Machado de Assis", "9788535910664", "29.90", 12, 1899, "Romance"),
                    livro("O Hobbit", "J. R. R. Tolkien", "9788595084742", "54.90", 7, 1937, "Fantasia"),
                    livro("Clean Code", "Robert C. Martin", "9780132350884", "119.00", 4, 2008, "Tecnologia"),
                    livro("Java 8 in Action", "Raoul-Gabriel Urma", "9781617291999", "189.50", 0, 2014, "Tecnologia"),
                    livro("Capitães da Areia", "Jorge Amado", "9788535914061", "39.90", 9, 1937, "Romance"),
                    livro("O Pequeno Príncipe", "Antoine de Saint-Exupéry", "9788595081512", "24.90", 15, 1943, "Infantil"),
                    livro("1984", "George Orwell", "9788535914849", "44.90", 6, 1949, "Ficção"),
                    livro("Sapiens", "Yuval Noah Harari", "9788525432186", "64.90", 3, 2011, "História")
            ).forEach(livroRepository::save);
        }
    }

    private static Livro livro(String titulo, String autor, String isbn, String preco,
                               int estoque, int ano, String genero) {
        return new Livro(titulo, autor, isbn, new BigDecimal(preco), estoque, ano, genero);
    }
}
