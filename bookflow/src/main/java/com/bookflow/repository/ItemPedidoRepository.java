package com.bookflow.repository;

import com.bookflow.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    boolean existsByLivroId(Long livroId);
}
