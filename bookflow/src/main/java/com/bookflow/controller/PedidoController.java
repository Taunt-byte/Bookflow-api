package com.bookflow.controller;

import com.bookflow.dto.NovoPedido;
import com.bookflow.dto.PedidoResposta;
import com.bookflow.model.StatusPedido;
import com.bookflow.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    // ----- CLIENTE -----

    /** @AuthenticationPrincipal entrega quem está logado; o cliente não escolhe "em nome de quem" compra. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResposta criar(@AuthenticationPrincipal UserDetails usuario,
                                @RequestBody @Valid NovoPedido novo) {
        return service.criar(usuario.getUsername(), novo);
    }

    @GetMapping("/meus")
    public List<PedidoResposta> meus(@AuthenticationPrincipal UserDetails usuario) {
        return service.doCliente(usuario.getUsername());
    }

    // ----- VENDEDOR -----

    @GetMapping("/todos")
    public List<PedidoResposta> todos() {
        return service.todos();
    }

    @PutMapping("/{id}/status")
    public PedidoResposta atualizarStatus(@PathVariable Long id, @RequestParam StatusPedido status) {
        return service.atualizarStatus(id, status);
    }

    @GetMapping("/resumo")
    public Map<String, Object> resumo() {
        return service.resumo();
    }
}
