# BookFlow

Livraria online feita em **Java com Spring Boot**: vitrine de livros, carrinho, login, pedidos com baixa de estoque e um painel onde só o vendedor edita o catálogo.

O projeto nasceu como um CRUD simples de livros e evoluiu para uma loja completa. As duas etapas estão no repositório, para dar para comparar o antes e o depois.

![Java 17+](https://img.shields.io/badge/Java-17%2B-red)
![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3.5-6db33f)
![Licença MIT](https://img.shields.io/badge/licen%C3%A7a-MIT-blue)

<p align="center">
  <img src="docs/bookflow-loja.png" alt="Página inicial da BookFlow no computador, com a estante de livros e o catálogo" width="640">
  <img src="docs/bookflow-celular.png" alt="Página inicial da BookFlow no celular" width="180">
</p>

## O que a loja faz

**Para quem compra**
- Navega pelo catálogo, busca por título ou autor, filtra por gênero e ordena por título, preço ou lançamento.
- Monta o carrinho sem precisar de conta. O login só é pedido na hora de finalizar.
- Cria a própria conta, finaliza a compra e acompanha o status de cada pedido.

**Para quem vende**
- Cadastra, edita e exclui livros. Essas ações são **exclusivas do vendedor**: a API recusa qualquer outra pessoa.
- Acompanha todos os pedidos e muda o status: realizado, enviado, entregue ou cancelado.
- Vê o resumo da loja: valor em estoque, livros esgotados, faturamento e mais vendidos.

**Regras de negócio**
- A compra baixa o estoque. Se um item não tiver estoque suficiente, o pedido inteiro é recusado e nada muda.
- Cada item guarda o preço do momento da compra, então mudar o preço depois não altera pedidos antigos.
- Cancelar um pedido devolve os livros ao estoque.
- Livro que já foi vendido não pode ser excluído, para preservar o histórico.
- As senhas são guardadas criptografadas com BCrypt.

## Tecnologias

| Camada | Uso |
|---|---|
| Java 17+ | Records, text blocks, lambdas, Streams e `Optional` |
| Spring Boot 3.3 | Web (API REST), Data JPA, Validation |
| Spring Security | Login por sessão, papéis `VENDEDOR` e `CLIENTE`, senhas com BCrypt |
| H2 | Banco em memória, recriado a cada inicialização |
| HTML, CSS e JavaScript | Páginas da loja e do painel, sem framework, consumindo a API com `fetch` |
| JUnit 5 + MockMvc | Testes de permissões e de pedidos |

## Estrutura do repositório

| Pasta | Conteúdo |
|---|---|
| [`01-crud-loja-livros`](01-crud-loja-livros) | Primeira etapa: CRUD de livros com API REST, validação e uma tela simples |
| [`02-bookflow`](02-bookflow) | A loja BookFlow completa, com login, carrinho, pedidos e painel do vendedor |
| [`docs`](docs) | Imagens usadas neste README |

Cada etapa é um projeto Maven independente e tem um README próprio com mais detalhes.

## Como rodar

Você precisa do **JDK 17 ou mais novo** e do **Maven**.

```bash
git clone https://github.com/Taunt-byte/Bookflow-api.git
cd Bookflow-api/02-bookflow
mvn spring-boot:run
```

Quando aparecer `Started BookflowApplication`, abra:

| Endereço | O que é |
|---|---|
| http://localhost:8080 | A loja |
| http://localhost:8080/painel.html | Painel do vendedor |
| http://localhost:8080/h2-console | Console do banco (JDBC URL `jdbc:h2:mem:bookflow`, usuário `sa`, sem senha) |

A aplicação já sobe com 8 livros e duas contas de teste:

| Conta | E-mail | Senha |
|---|---|---|
| Vendedor | `vendedor@bookflow.com` | `vendedor123` |
| Cliente | `cliente@bookflow.com` | `cliente123` |

> As duas etapas usam a porta 8080, então rode uma de cada vez.

## Quem pode fazer o quê

| Ação | Visitante | Cliente | Vendedor |
|---|:-:|:-:|:-:|
| Ver o catálogo | ✅ | ✅ | ✅ |
| Comprar e ver os próprios pedidos | ❌ | ✅ | ❌ |
| Cadastrar, editar e excluir livros | ❌ | ❌ | ✅ |
| Ver todos os pedidos e mudar status | ❌ | ❌ | ✅ |
| Ver relatórios da loja | ❌ | ❌ | ✅ |

As regras ficam em [`SecurityConfig.java`](02-bookflow/src/main/java/com/bookflow/config/SecurityConfig.java). A proteção está na API: mesmo chamando os endpoints direto, sem passar pelas telas, o servidor responde `401` (não logado) ou `403` (sem permissão).

## API

| Método | Endpoint | Acesso |
|---|---|---|
| `POST` | `/api/auth/cadastro` | Público (cria conta de cliente) |
| `POST` | `/api/auth/login` · `/api/auth/logout` | Público · logado |
| `GET` | `/api/auth/me` | Público (informa quem está logado) |
| `GET` | `/api/livros` · `/api/livros/{id}` | Público |
| `POST` `PUT` `DELETE` | `/api/livros` · `/api/livros/{id}` | Vendedor |
| `GET` | `/api/livros/resumo` · `/api/livros/sem-estoque` | Vendedor |
| `POST` | `/api/pedidos` | Cliente |
| `GET` | `/api/pedidos/meus` | Cliente |
| `GET` | `/api/pedidos/todos` · `/api/pedidos/resumo` | Vendedor |
| `PUT` | `/api/pedidos/{id}/status?status=ENVIADO` | Vendedor |

Exemplo de pedido:

```json
{ "itens": [ { "livroId": 1, "quantidade": 2 }, { "livroId": 3, "quantidade": 1 } ] }
```

## Programação funcional no código

O repositório começou como estudo de programação funcional no Java 8, e esse estilo aparece em todas as regras da loja. Alguns trechos, levemente simplificados:

Somar o mesmo livro que aparece duas vezes no carrinho (`PedidoService`):

```java
Map<Long, Integer> quantidadePorLivro = novo.itens().stream()
        .collect(Collectors.groupingBy(
                NovoPedido.Item::livroId,
                LinkedHashMap::new,
                Collectors.summingInt(NovoPedido.Item::quantidade)));
```

Calcular o faturamento ignorando pedidos cancelados:

```java
BigDecimal faturamento = pedidos.stream()
        .filter(p -> p.getStatus() != StatusPedido.CANCELADO)
        .map(Pedido::getTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
```

Buscar um livro ou responder com erro 404, usando `Optional` (`LivroService`):

```java
return repository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Livro " + id + " não encontrado"));
```

Para estudar mais, veja as classes `*Service` em [`02-bookflow/src/main/java/com/bookflow/service`](02-bookflow/src/main/java/com/bookflow/service).

## Testes

```bash
cd 02-bookflow
mvn test
```

Os testes conferem as permissões de visitante, cliente e vendedor, a compra baixando o estoque e a recusa de livros esgotados.

## Observações

- O banco H2 fica em memória: ao reiniciar a aplicação, tudo volta aos dados iniciais.
- A proteção CSRF do Spring Security está desativada para simplificar o estudo. Para publicar a loja de verdade, o ideal é reativá-la ou trocar a sessão por autenticação com token.

## Licença

Distribuído sob a licença [MIT](LICENSE).
