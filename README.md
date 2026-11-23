# Curso Java 8 · Programação Funcional

Estudos de Java com Spring Boot, usando bastante lambdas, Streams e `Optional` (os recursos que chegaram no Java 8).

O repositório tem dois projetos, em ordem de evolução:

| Pasta | O que é | Para que serve |
|---|---|---|
| [`01-crud-loja-livros`](01-crud-loja-livros) | CRUD simples de livros: cadastrar, listar, editar e excluir | Primeiro contato com Spring Boot, JPA, validação e API REST |
| [`02-bookflow`](02-bookflow) | **BookFlow**, a loja completa: vitrine, carrinho, login, pedidos e painel do vendedor | Evolução do CRUD com Spring Security, papéis (vendedor e cliente) e regras de negócio |

Cada pasta é um projeto Maven independente e tem seu próprio README.
Os dois usam a porta 8080, então rode um de cada vez.

```bash
cd 02-bookflow
mvn spring-boot:run
```

Requisitos: JDK 17 ou mais novo e Maven.

## Sobre o Java 8 e as lambdas

O Java 8 trouxe uma nova sintaxe: funções lambda! À primeira vista, pode parecer complicada, mas na verdade é muito mais simples do que o que tínhamos antes. Nos projetos, procure por `stream()`, `filter`, `map`, `reduce`, `Collectors.groupingBy` e `Optional`, principalmente nas classes `*Service`.
