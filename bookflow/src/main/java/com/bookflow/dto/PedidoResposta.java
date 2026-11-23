package com.bookflow.dto;

import com.bookflow.model.Pedido;
import com.bookflow.model.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResposta(Long id, String cliente, LocalDateTime dataHora,
                             StatusPedido status, BigDecimal total, List<Item> itens) {

    public record Item(Long livroId, String titulo, int quantidade,
                       BigDecimal precoUnitario, BigDecimal subtotal) {
    }

    /** Converte a entidade Pedido nesta resposta, usando stream + map nos itens. */
    public static PedidoResposta de(Pedido p) {
        List<Item> itens = p.getItens().stream()
                .map(i -> new Item(i.getLivro().getId(), i.getLivro().getTitulo(), i.getQuantidade(),
                        i.getPrecoUnitario(), i.getSubtotal()))
                .toList();
        return new PedidoResposta(p.getId(), p.getCliente().getNome(), p.getDataHora(),
                p.getStatus(), p.getTotal(), itens);
    }
}
