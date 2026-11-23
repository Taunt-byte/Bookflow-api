# 02 · BookFlow

Loja de livros feita com **Spring Boot**, evoluída a partir do CRUD da pasta `01-crud-loja-livros`.

- **Visitante:** vê o catálogo e monta o carrinho.
- **Cliente:** cria conta, faz login, compra e acompanha os próprios pedidos.
- **Vendedor:** é o único que cadastra, edita e exclui livros, e cuida dos pedidos (enviar, entregar, cancelar).

## Como rodar

Requisitos: JDK 17+ e Maven.

```bash
cd 02-bookflow
mvn spring-boot:run
```

> Se o `01-crud-loja-livros` ainda estiver rodando, pare ele antes (Ctrl + C no terminal), porque os dois usam a porta 8080.

Abra:
- **Loja:** http://localhost:8080
- **Painel do vendedor:** http://localhost:8080/painel.html
- **Banco H2:** http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:bookflow`, usuário `sa`, sem senha)

### Contas de teste

| Tipo | E-mail | Senha |
|---|---|---|
| Vendedor | `vendedor@bookflow.com` | `vendedor123` |
| Cliente | `cliente@bookflow.com` | `cliente123` |

Também dá para criar uma conta de cliente nova pelo botão **Entrar → Cadastre-se**.
O banco é em memória: ao reiniciar a aplicação, tudo volta ao estado inicial.

## Quem pode fazer o quê

| Ação | Visitante | Cliente | Vendedor |
|---|:-:|:-:|:-:|
| Ver livros (`GET /api/livros`) | ✅ | ✅ | ✅ |
| Cadastrar / editar / excluir livro | ❌ 401 | ❌ 403 | ✅ |
| Comprar (`POST /api/pedidos`) | ❌ 401 | ✅ | ❌ 403 |
| Ver os próprios pedidos (`/api/pedidos/meus`) | ❌ | ✅ | ❌ |
| Ver todos os pedidos e mudar status | ❌ | ❌ | ✅ |
| Relatórios (`/api/livros/resumo`, `/api/pedidos/resumo`) | ❌ | ❌ | ✅ |

Essas regras ficam em `config/SecurityConfig.java`. **401** = não está logado; **403** = está logado, mas não tem permissão.

A proteção de verdade está na API (no Java), não nas telas: mesmo que alguém abra o `painel.html` ou chame a API direto, o servidor recusa.

## Regras da loja

- Comprar baixa o estoque. Se algum livro não tiver estoque suficiente, **o pedido inteiro é recusado** e nada muda (`@Transactional`).
- O item do pedido guarda o **preço do momento da compra**; mudar o preço do livro depois não altera pedidos antigos.
- **Cancelar** um pedido devolve os livros ao estoque. Pedido cancelado não muda mais de status.
- Livro que já foi vendido não pode ser excluído (para não estragar o histórico); zere o estoque para tirá-lo de venda.
- Senhas são salvas criptografadas com **BCrypt**.

## Endpoints

| Método | URL | Quem |
|---|---|---|
| POST | `/api/auth/cadastro` | todos (cria cliente) |
| POST | `/api/auth/login` (form: `email`, `senha`) | todos |
| POST | `/api/auth/logout` | logado |
| GET | `/api/auth/me` | todos (diz quem está logado) |
| GET | `/api/livros?busca=` | todos |
| GET | `/api/livros/{id}` | todos |
| POST/PUT/DELETE | `/api/livros`, `/api/livros/{id}` | vendedor |
| GET | `/api/livros/resumo`, `/api/livros/sem-estoque` | vendedor |
| POST | `/api/pedidos` | cliente |
| GET | `/api/pedidos/meus` | cliente |
| GET | `/api/pedidos/todos`, `/api/pedidos/resumo` | vendedor |
| PUT | `/api/pedidos/{id}/status?status=ENVIADO` | vendedor |

Exemplo de pedido:
```json
{ "itens": [ { "livroId": 1, "quantidade": 2 }, { "livroId": 3, "quantidade": 1 } ] }
```

## Estrutura

```
src/main/java/com/bookflow/
├── BookflowApplication.java
├── config/
│   ├── SecurityConfig.java      # login, senhas e permissões
│   └── DadosIniciais.java       # cria contas de teste e livros ao iniciar
├── model/                       # entidades: Livro, Usuario, Pedido, ItemPedido + enums
├── repository/                  # acesso ao banco (Spring Data JPA)
├── dto/                         # records: o que entra e sai da API (nunca expõe senha)
├── service/                     # regras de negócio (com Streams e lambdas)
├── controller/                  # endpoints REST
└── exception/                   # erros em JSON
src/main/resources/static/
├── index.html                   # a loja
├── painel.html                  # painel do vendedor
├── css/bookflow.css
└── js/comum.js                  # funções usadas pelas duas telas
```

## Testes

```bash
mvn test
```

Os testes conferem as permissões (visitante, cliente e vendedor), a compra baixando o estoque e a recusa de livro esgotado.

## Observação

A proteção CSRF do Spring Security está desligada para deixar o projeto mais simples de estudar.
Num site publicado de verdade, o ideal é religá-la (ou usar autenticação por token, como JWT).
