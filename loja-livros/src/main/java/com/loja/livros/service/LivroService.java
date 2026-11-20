package com.loja.livros.service;

import com.loja.livros.exception.RecursoNaoEncontradoException;
import com.loja.livros.exception.RegraNegocioException;
import com.loja.livros.model.Livro;
import com.loja.livros.repository.LivroRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LivroService {

    private final LivroRepository repository;

    public LivroService(LivroRepository repository) {
        this.repository = repository;
    }

    // ---------- CRUD ----------

    public List<Livro> listar(String busca) {
        if (busca == null || busca.isBlank()) {
            return repository.findAll();
        }
        return repository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCase(busca, busca);
    }

    public Livro buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro " + id + " não encontrado"));
    }

    public Livro criar(Livro livro) {
        if (repository.existsByIsbn(livro.getIsbn())) {
            throw new RegraNegocioException("Já existe um livro com o ISBN " + livro.getIsbn());
        }
        livro.setId(null);
        return repository.save(livro);
    }

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

    public void excluir(Long id) {
        repository.delete(buscarPorId(id));
    }

    // ---------- Extras com Streams / lambdas (Java 8+) ----------

    /** Livros com estoque zerado. */
    public List<Livro> semEstoque() {
        return repository.findAll().stream()
                .filter(l -> l.getEstoque() == 0)
                .collect(Collectors.toList());
    }

    /** Resumo da loja: totais, valor do estoque, média de preço e livros por gênero. */
    public Map<String, Object> resumo() {
        List<Livro> livros = repository.findAll();

        BigDecimal valorEstoque = livros.stream()
                .map(l -> l.getPreco().multiply(BigDecimal.valueOf(l.getEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double precoMedio = livros.stream()
                .mapToDouble(l -> l.getPreco().doubleValue())
                .average()
                .orElse(0);

        Map<String, Long> porGenero = livros.stream()
                .collect(Collectors.groupingBy(
                        l -> Optional.ofNullable(l.getGenero()).orElse("Sem gênero"),
                        TreeMap::new,
                        Collectors.counting()));

        Optional<String> maisCaro = livros.stream()
                .max(Comparator.comparing(Livro::getPreco))
                .map(Livro::getTitulo);

        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("totalTitulos", livros.size());
        resumo.put("totalExemplares", livros.stream().mapToInt(Livro::getEstoque).sum());
        resumo.put("valorEmEstoque", valorEstoque);
        resumo.put("precoMedio", Math.round(precoMedio * 100) / 100.0);
        resumo.put("livroMaisCaro", maisCaro.orElse("-"));
        resumo.put("porGenero", porGenero);
        return resumo;
    }
}
