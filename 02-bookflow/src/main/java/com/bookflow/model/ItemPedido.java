package com.bookflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Livro livro;

    @Column(nullable = false)
    private Integer quantidade;

    /** Preço no momento da compra: se o vendedor mudar o preço depois, o pedido não muda. */
    @Column(nullable = false)
    private BigDecimal precoUnitario;

    public ItemPedido() { }

    public ItemPedido(Pedido pedido, Livro livro, Integer quantidade, BigDecimal precoUnitario) {
        this.pedido = pedido;
        this.livro = livro;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public Livro getLivro() { return livro; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
}
