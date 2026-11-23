package com.bookflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados que o cliente envia para criar a conta. */
public record CadastroRequest(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha precisa ter pelo menos 6 caracteres") String senha) {
}
