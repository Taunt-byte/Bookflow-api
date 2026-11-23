package com.loja.livros;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // desfaz as alterações após cada teste
class LivroControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listaOsLivrosIniciais() throws Exception {
        mvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void criaLivroValido() throws Exception {
        String json = """
                {"titulo":"Vidas Secas","autor":"Graciliano Ramos","isbn":"9788501065374",
                 "preco":34.90,"estoque":3,"anoPublicacao":1938,"genero":"Romance"}
                """;
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void recusaLivroInvalido() throws Exception {
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retorna404ParaLivroInexistente() throws Exception {
        mvc.perform(get("/api/livros/999")).andExpect(status().isNotFound());
    }
}
