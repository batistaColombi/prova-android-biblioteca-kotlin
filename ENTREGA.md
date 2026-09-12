# Entrega

## Tarefa 1 — disponibilidade no `listar`

O `listar` mostrava quantos exemplares a biblioteca tem no total. O pedido era mostrar
quantos estão livres agora, sem que emprestar um exemplar de Duna faça os outros dois
sumirem da listagem.

## Como pensei

Criei um método que, para um livro, conta quantos empréstimos ativos existem para aquele
livro e subtrai do total de cópias. O que sobra é o que está na prateleira.

```kotlin
fun availableCopies(bookId: Int): Int {
    val book = requireNotNull(library.findBook(bookId)) { "Livro $bookId não encontrado." }
    val activeLoans = library.loans.count { it.bookId == bookId }
    return (book.copies - activeLoans).coerceAtLeast(0)
}
```

Preferi calcular na hora em vez de guardar um campo "disponíveis" no `Book`: o campo seria
uma segunda fonte de verdade, e qualquer empréstimo que esquecesse de atualizá-lo deixaria
o acervo mentindo. Assim `Book.copies` é o total e a lista de empréstimos é o que está fora.

O método fica no service e só devolve número, quem monta a tabela é o `Main.kt` — é a
convenção que o README pede.

## O que mudei no que já existia

- `LibraryService.availableCopies`: implementei o `TODO`.
- `Main.kt` / `showCatalog`: a coluna "exemplares" mostrava `book.copies`. Troquei por duas
  colunas, `livres` e `total`. Mostrar os dois deixa explícito o ponto do enunciado: `Duna`
  aparece como `livres = 1` e `total = 3`, e o título não desaparece da listagem.

## Tarefa 2 — `buscar <termo>`

O comando precisava procurar por título, autor ou gênero, no mesmo formato do `listar`,
sem depender de maiúsculas/minúsculas nem de acentos (`solidao` acha `Solidão`). Se não
houvesse resultado, avisar em vez de mostrar tabela vazia.

## Como pensei

A busca ficou no `LibraryService`, que devolve a lista de livros. O `Main.kt` só monta a
tela. Assim a regra não imprime nada — segue a arquitetura do projeto.

Para o termo bater com ou sem acento, normalizo o texto antes de comparar: decompõe os
caracteres (NFD), remove as marcas de acento e passa para minúsculas. Aí `contains`
funciona igual para `SARAMAGO` e `saramago`, e para `solidao` e `Solidão`.

```kotlin
fun search(term: String): List<Book> {
    val needle = normalize(term)
    if (needle.isEmpty()) return emptyList()

    return library.books.filter {
        normalize(it.title).contains(needle) ||
            normalize(it.author).contains(needle) ||
            normalize(it.genre).contains(needle)
    }
}
```

## O que mudei no que já existia

- `LibraryService.search` e `normalize`: implementei o TODO da busca.

- `Main.kt`: liguei o comando buscar a showSearch, no mesmo estilo do showCatalog.

## Tarefa 3 — 