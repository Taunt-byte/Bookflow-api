package com.bookflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** O carrinho enviado pelo cliente ao finalizar a compra. */
public record NovoPedido(
        @NotEmpty(message = "O carrinho está vazio") @Valid List<Item> itens) {

    public record Item(
            @NotNull(message = "Informe o livro") Long livroId,
            @NotNull @Min(value = 1, message = "A quantidade mínima é 1") Integer quantidade) {
    }
}
