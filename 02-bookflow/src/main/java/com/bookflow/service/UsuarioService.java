package com.bookflow.service;

import com.bookflow.dto.CadastroRequest;
import com.bookflow.exception.RegraNegocioException;
import com.bookflow.model.Papel;
import com.bookflow.model.Usuario;
import com.bookflow.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Todo cadastro feito pelo site vira CLIENTE. Vendedor só é criado pelo sistema. */
    @Transactional
    public Usuario cadastrarCliente(CadastroRequest dados) {
        String email = dados.email().trim().toLowerCase();
        if (repository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe uma conta com esse e-mail");
        }
        Usuario novo = new Usuario(dados.nome().trim(), email,
                passwordEncoder.encode(dados.senha()), Papel.CLIENTE);
        return repository.save(novo);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }
}
