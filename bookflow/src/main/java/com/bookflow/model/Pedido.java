package com.bookflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario cliente;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    // cascade = ALL: salvar o pedido salva os itens junto
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() { }

    public Pedido(Usuario cliente) {
        this.cliente = cliente;
        this.dataHora = LocalDateTime.now();
        this.status = StatusPedido.REALIZADO;
    }

    public void adicionarItem(Livro livro, int quantidade) {
        itens.add(new ItemPedido(this, livro, quantidade, livro.getPreco()));
    }

    /** Total calculado na hora, somando os subtotais dos itens. */
    public BigDecimal getTotal() {
        return itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public Usuario getCliente() { return cliente; }
    public LocalDateTime getDataHora() { return dataHora; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public List<ItemPedido> getItens() { return itens; }
}
