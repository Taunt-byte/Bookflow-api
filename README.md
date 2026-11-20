# Loja de Livros — CRUD com Spring Boot

API REST + página web simples para cadastrar, listar, editar e excluir livros.

**Tecnologias:** Java 17, Spring Boot 3.3, Spring Data JPA, Bean Validation, banco H2 (em memória).

## Como rodar

Requisitos: **JDK 17 ou mais novo** e **Maven** (ou abra a pasta no IntelliJ / VS Code, que já trazem o Maven).

```bash
cd loja-livros
mvn spring-boot:run
```

No IntelliJ: abra a pasta `loja-livros`, espere baixar as dependências e rode a classe `LojaLivrosApplication`.

Depois acesse:
- Tela da loja: http://localhost:8080
- Console do banco H2: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:lojadb`, usuário `sa`, sem senha)

O sistema já inicia com 5 livros de exemplo (`src/main/resources/data.sql`).

## Endpoints

| Método | URL | O que faz |
|---|---|---|
| GET | `/api/livros` | Lista todos (aceita `?busca=texto` por título/autor) |
| GET | `/api/livros/{id}` | Busca um livro |
| POST | `/api/livros` | Cadastra |
| PUT | `/api/livros/{id}` | Atualiza |
| DELETE | `/api/livros/{id}` | Exclui |
| GET | `/api/livros/sem-estoque` | Livros com estoque zerado |
| GET | `/api/livros/resumo` | Totais, valor em estoque, preço médio, livros por gênero |

Exemplo de JSON para POST/PUT:
```json
{ "titulo": "Vidas Secas", "autor": "Graciliano Ramos", "isbn": "9788501065374",
  "preco": 34.90, "estoque": 3, "anoPublicacao": 1938, "genero": "Romance" }
```

## Estrutura

```
src/main/java/com/loja/livros/
├── LojaLivrosApplication.java   # classe principal
├── model/Livro.java             # entidade JPA com validações
├── repository/LivroRepository.java
├── service/LivroService.java    # regras + relatórios com Streams/lambdas
├── controller/LivroController.java
└── exception/                   # erros 404, 409 e 400 em JSON
```

O `LivroService` usa bastante programação funcional do Java 8 (`stream`, `filter`, `map`, `reduce`,
`Collectors.groupingBy`, `Optional`, method references) — combina com o tema do curso.

## Testes

```bash
mvn test
```
