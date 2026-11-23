package com.bookflow.controller;

import com.bookflow.dto.CadastroRequest;
import com.bookflow.dto.UsuarioResposta;
import com.bookflow.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Login (POST /api/auth/login) e logout (POST /api/auth/logout) são tratados
 * direto pelo Spring Security, configurados em SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrar(@RequestBody @Valid CadastroRequest dados) {
        return UsuarioResposta.de(usuarioService.cadastrarCliente(dados));
    }

    /** Diz ao site quem está logado (ou que ninguém está). */
    @GetMapping("/me")
    public UsuarioResposta me(@AuthenticationPrincipal UserDetails usuario) {
        if (usuario == null) {
            return UsuarioResposta.anonimo();
        }
        return usuarioService.buscarPorEmail(usuario.getUsername())
                .map(UsuarioResposta::de)
                .orElse(UsuarioResposta.anonimo());
    }
}
