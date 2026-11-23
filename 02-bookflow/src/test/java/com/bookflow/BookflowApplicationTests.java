package com.bookflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // desfaz as alterações no banco depois de cada teste
class BookflowApplicationTests {

    @Autowired
    MockMvc mvc;

    static final String LIVRO_NOVO = """
            {"titulo":"Vidas Secas","autor":"Graciliano Ramos","isbn":"9788501065374",
             "preco":34.90,"estoque":3,"anoPublicacao":1938,"genero":"Romance"}
            """;

    @Test
    void visitanteVeOCatalogo() throws Exception {
        mvc.perform(get("/api/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8));
    }

    @Test
    void visitanteNaoPodeCadastrarLivro() throws Exception {
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(LIVRO_NOVO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNaoPodeCadastrarLivro() throws Exception {
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(LIVRO_NOVO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void vendedorCadastraLivro() throws Exception {
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(LIVRO_NOVO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "cliente@bookflow.com", roles = "CLIENTE")
    void clienteCompraEOEstoqueBaixa() throws Exception {
        // Livro 1 = Dom Casmurro, R$ 29,90, 12 em estoque
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itens\":[{\"livroId\":1,\"quantidade\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(59.8));

        mvc.perform(get("/api/livros/1"))
                .andExpect(jsonPath("$.estoque").value(10));
    }

    @Test
    @WithMockUser(username = "cliente@bookflow.com", roles = "CLIENTE")
    void naoVendeLivroSemEstoque() throws Exception {
        // Livro 4 = Java 8 in Action, estoque 0
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itens\":[{\"livroId\":4,\"quantidade\":1}]}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void vendedorNaoCompra() throws Exception {
        mvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itens\":[{\"livroId\":1,\"quantidade\":1}]}"))
                .andExpect(status().isForbidden());
    }
}
