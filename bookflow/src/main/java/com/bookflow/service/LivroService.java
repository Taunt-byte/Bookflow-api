package com.bookflow.service;

import com.bookflow.exception.RecursoNaoEncontradoException;
import com.bookflow.exception.RegraNegocioException;
import com.bookflow.model.Livro;
import com.bookflow.repository.ItemPedidoRepository;
import com.bookflow.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LivroService {

    private final LivroRepository repository;
    private final ItemPedidoRepository itemPedidoRepository;

    public LivroService(LivroRepository repository, ItemPedidoRepository itemPedidoRepository) {
        this.repository = repository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    // ---------- CRUD ----------

    public List<Livro> listar(String busca) {
        List<Livro> livros = (busca == null || busca.isBlank())
                ? repository.findAll()
                : repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(busca, busca);
        // Ordena por título usando method reference
        return livros.stream()
                .sorted(Comparator.comparing(Livro::getTitulo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Livro buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro " + id + " não encontrado"));
    }

    @Transactional
    public Livro criar(Livro livro) {
        if (repository.existsByIsbn(livro.getIsbn())) {
            throw new RegraNegocioException("Já existe um livro com o ISBN " + livro.getIsbn());
        }
        livro.setId(null);
        return repository.save(livro);
    }

    @Transactional
    public Livro atualizar(Long id, Livro dados) {
        Livro existente = buscarPorId(id);
        if (repository.existsByIsbnAndIdNot(dados.getIsbn(), id)) {
            throw new RegraNegocioException("Já existe outro livro com o ISBN " + dados.getIsbn());
        }
        existente.setTitulo(dados.getTitulo());
        existente.setAutor(dados.getAutor());
        existente.setIsbn(dados.getIsbn());
        existente.setPreco(dados.getPreco());
        existente.setEstoque(dados.getEstoque());
        existente.setAnoPublicacao(dados.getAnoPublicacao());
        existente.setGenero(dados.getGenero());
        return repository.save(existente);
    }

    @Transactional
    public void excluir(Long id) {
        Livro livro = buscarPorId(id);
        if (itemPedidoRepository.existsByLivroId(id)) {
            throw new RegraNegocioException(
                    "\"" + livro.getTitulo() + "\" já aparece em pedidos e não pode ser excluído. Zere o estoque para tirá-lo de venda.");
        }
        repository.delete(livro);
    }

    // ---------- Relatórios com Streams ----------

    public List<Livro> semEstoque() {
        return repository.findAll().stream()
                .filter(l -> l.getEstoque() == 0)
                .toList();
    }

    public Map<String, Object> resumo() {
        List<Livro> livros = repository.findAll();

        BigDecimal valorEstoque = livros.stream()
                .map(l -> l.getPreco().multiply(BigDecimal.valueOf(l.getEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> porGenero = livros.stream()
                .collect(Collectors.groupingBy(
                        l -> Optional.ofNullable(l.getGenero()).filter(g -> !g.isBlank()).orElse("Sem gênero"),
                        TreeMap::new,
                        Collectors.counting()));

        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("totalTitulos", livros.size());
        resumo.put("totalExemplares", livros.stream().mapToInt(Livro::getEstoque).sum());
        resumo.put("valorEmEstoque", valorEstoque);
        resumo.put("semEstoque", livros.stream().filter(l -> l.getEstoque() == 0).count());
        resumo.put("porGenero", porGenero);
        return resumo;
    }
}
