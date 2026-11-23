package com.bookflow.config;

import com.bookflow.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Aqui fica QUEM pode fazer O QUÊ.
 * As regras são lidas de cima para baixo: a primeira que combinar com a URL vale.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF desligado para simplificar o projeto de estudo (ver README)
            .csrf(csrf -> csrf.disable())
            // Permite o console do H2, que usa frames
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            .authorizeHttpRequests(auth -> auth
                // Relatórios de livros: só vendedor
                .requestMatchers(antMatcher("/api/livros/resumo"), antMatcher("/api/livros/sem-estoque"))
                    .hasRole("VENDEDOR")
                // Ver o catálogo: qualquer pessoa
                .requestMatchers(antMatcher(HttpMethod.GET, "/api/livros"), antMatcher(HttpMethod.GET, "/api/livros/*"))
                    .permitAll()
                // Criar, editar e excluir livros: só vendedor
                .requestMatchers(antMatcher("/api/livros"), antMatcher("/api/livros/**"))
                    .hasRole("VENDEDOR")
                // Gerenciar pedidos: só vendedor
                .requestMatchers(antMatcher("/api/pedidos/todos"), antMatcher("/api/pedidos/resumo"),
                                 antMatcher("/api/pedidos/*/status"))
                    .hasRole("VENDEDOR")
                // Comprar e ver os próprios pedidos: só cliente
                .requestMatchers(antMatcher("/api/pedidos"), antMatcher("/api/pedidos/**"))
                    .hasRole("CLIENTE")
                // Páginas, CSS, JS, cadastro, /me, console H2...
                .anyRequest().permitAll())

            // Login por formulário, respondendo com códigos HTTP em vez de redirecionar
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .successHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
                .failureHandler((req, res, ex) -> responderJson(res, 401, "E-mail ou senha inválidos")))

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK)))

            .exceptionHandling(ex -> ex
                // Não está logado
                .authenticationEntryPoint((req, res, e) -> responderJson(res, 401, "Faça login para continuar"))
                // Está logado, mas não tem permissão (ex.: cliente tentando editar livro)
                .accessDeniedHandler((req, res, e) -> responderJson(res, 403, "Você não tem permissão para isso")));

        return http.build();
    }

    /** Ensina o Spring a achar um usuário pelo e-mail no banco. */
    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository repository) {
        return email -> repository.findByEmail(email.trim().toLowerCase())
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getSenha())
                        .roles(u.getPapel().name())   // vira ROLE_VENDEDOR ou ROLE_CLIENTE
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    /** BCrypt embaralha a senha antes de salvar; nem quem olha o banco descobre a senha. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void responderJson(HttpServletResponse res, int status, String mensagem) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
