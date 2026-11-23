package com.bookflow.service;

import com.bookflow.dto.NovoPedido;
import com.bookflow.dto.PedidoResposta;
import com.bookflow.exception.RecursoNaoEncontradoException;
import com.bookflow.exception.RegraNegocioException;
import com.bookflow.model.*;
import com.bookflow.repository.LivroRepository;
import com.bookflow.repository.PedidoRepository;
import com.bookflow.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, LivroRepository livroRepository,
                         UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Cria o pedido e baixa o estoque. É @Transactional: se qualquer livro
     * não tiver estoque, NADA é salvo (nem os livros que já tinham sido baixados).
     */
    @Transactional
    public PedidoResposta criar(String emailCliente, NovoPedido novo) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        // Se o mesmo livro vier duas vezes no carrinho, soma as quantidades
        Map<Long, Integer> quantidadePorLivro = novo.itens().stream()
                .collect(Collectors.groupingBy(
                        NovoPedido.Item::livroId,
                        LinkedHashMap::new,
                        Collectors.summingInt(NovoPedido.Item::quantidade)));

        Pedido pedido = new Pedido(cliente);
        quantidadePorLivro.forEach((livroId, quantidade) -> {
            Livro livro = livroRepository.findById(livroId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Livro " + livroId + " não encontrado"));
            if (livro.getEstoque() < quantidade) {
                throw new RegraNegocioException("Estoque insuficiente para \"" + livro.getTitulo()
                        + "\" (disponível: " + livro.getEstoque() + ")");
            }
            livro.setEstoque(livro.getEstoque() - quantidade);
            pedido.adicionarItem(livro, quantidade);
        });

        return PedidoResposta.de(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResposta> doCliente(String emailCliente) {
        return pedidoRepository.findByClienteEmailOrderByDataHoraDesc(emailCliente).stream()
                .map(PedidoResposta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResposta> todos() {
        return pedidoRepository.findAllByOrderByDataHoraDesc().stream()
                .map(PedidoResposta::de)
                .toList();
    }

    /** Vendedor muda o status. Cancelar devolve os livros ao estoque. */
    @Transactional
    public PedidoResposta atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido " + id + " não encontrado"));

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RegraNegocioException("Um pedido cancelado não pode mudar de status");
        }
        if (novoStatus == StatusPedido.CANCELADO) {
            pedido.getItens().forEach(item -> {
                Livro livro = item.getLivro();
                livro.setEstoque(livro.getEstoque() + item.getQuantidade());
            });
        }
        pedido.setStatus(novoStatus);
        return PedidoResposta.de(pedido);
    }

    /** Números de vendas para o painel do vendedor. */
    @Transactional(readOnly = true)
    public Map<String, Object> resumo() {
        List<Pedido> validos = pedidoRepository.findAll().stream()
                .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
                .toList();

        BigDecimal faturamento = validos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer> vendidosPorTitulo = validos.stream()
                .flatMap(p -> p.getItens().stream())
                .collect(Collectors.groupingBy(
                        i -> i.getLivro().getTitulo(),
                        Collectors.summingInt(ItemPedido::getQuantidade)));

        List<Map<String, Object>> maisVendidos = vendidosPorTitulo.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(e -> Map.<String, Object>of("titulo", e.getKey(), "quantidade", e.getValue()))
                .toList();

        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("totalPedidos", validos.size());
        resumo.put("faturamento", faturamento);
        resumo.put("maisVendidos", maisVendidos);
        return resumo;
    }
}
