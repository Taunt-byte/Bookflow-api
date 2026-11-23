package com.bookflow.dto;

import com.bookflow.model.Papel;
import com.bookflow.model.Usuario;

/** O que o site recebe sobre o usuário logado (nunca a senha). */
public record UsuarioResposta(boolean logado, String nome, String email, Papel papel) {

    public static UsuarioResposta de(Usuario u) {
        return new UsuarioResposta(true, u.getNome(), u.getEmail(), u.getPapel());
    }

    public static UsuarioResposta anonimo() {
        return new UsuarioResposta(false, null, null, null);
    }
}
