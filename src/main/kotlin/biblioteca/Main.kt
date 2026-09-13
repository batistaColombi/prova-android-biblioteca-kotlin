package biblioteca

import biblioteca.cli.Command
import biblioteca.cli.Console
import biblioteca.data.Library
import biblioteca.service.LibraryService
import kotlin.text.Charsets

fun main() {
    System.setOut(java.io.PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(java.io.PrintStream(System.err, true, Charsets.UTF_8))
    val service = LibraryService(Library())

    Console.title("Biblioteca")
    Console.info("Digite 'ajuda' para ver os comandos, 'sair' para encerrar.")

    while (true) {
        print("\n> ")
        val line = readlnOrNull() ?: break
        val command = Command.parse(line) ?: continue

        when (command.name) {
            "ajuda" -> showHelp()
            "listar" -> showCatalog(service)
            "buscar" -> showSearch(service, command)
            "emprestar" -> showBorrow(service, command)
            "devolver" -> showReturn(service, command)
            "sair" -> {
                Console.info("Até mais.")
                return
            }

            // TODO (Tarefas 2 a 4): implementar os comandos novos aqui.
            "membro" ->
                Console.error("comando '${command.name}' ainda não implementado")

            else -> Console.error("não conheço o comando '${command.name}'. Tente 'ajuda'.")
        }
    }
}

private fun showHelp() {
    Console.title("Comandos")
    Console.table(
        headers = listOf("comando", "o que faz"),
        rows = listOf(
            listOf("listar", "mostra o acervo"),
            listOf("buscar <termo>", "procura por título, autor ou gênero"),
            listOf("emprestar <livro> <membro>", "empresta um exemplar a um membro"),
            listOf("devolver <livro> <membro>", "devolve um exemplar"),
            listOf("membro <id>", "mostra os empréstimos de um membro"),
            listOf("ajuda", "mostra esta lista"),
            listOf("sair", "encerra o programa"),
        ),
    )
}

/**
 * Comando de referência: se ficar em dúvida sobre estilo, copie o que está aqui.
 *
 * A coluna `livres` vem do service; `total` fica ao lado para deixar claro que
 * emprestar um exemplar não remove o título do acervo.
 */
private fun showCatalog(service: LibraryService) {
    val books = service.catalog()

    Console.title("Acervo")
    Console.table(
        headers = listOf("id", "título", "autor", "gênero", "livres", "total"),
        rows = books.map { book ->
            listOf(
                book.id.toString(),
                book.title,
                book.author,
                book.genre,
                service.availableCopies(book.id).toString(),
                book.copies.toString(),
            )
        }
    )
}

/**
 * Busca no acervo e mostra no mesmo formato do `listar`.
 * Termo vazio ou sem resultado viram mensagem; a regra da busca fica no service.
 */
private fun showSearch(service: LibraryService, command: Command) {
    val term = command.arguments.joinToString(" ").trim()
    if (term.isEmpty()) {
        Console.error("uso: buscar <termo>")
        return
    }

    val books = service.search(term)
    if (books.isEmpty()) {
        Console.info("Nenhum livro encontrado para \"$term\".")
        return
    }

    Console.title("Busca")
    Console.table(
        headers = listOf("id", "título", "autor", "gênero", "livres", "total"),
        rows = books.map { book ->
            listOf(
                book.id.toString(),
                book.title,
                book.author,
                book.genre,
                service.availableCopies(book.id).toString(),
                book.copies.toString(),
            )
        },
    )
}

/**
 * Empresta um exemplar. A regra fica no service; aqui só leio os ids e mostro o resultado.
 */
private fun showBorrow(service: LibraryService, command: Command) {
    val bookId = command.argument(0)?.toIntOrNull()
    val memberId = command.argument(1)?.toIntOrNull()
    if (bookId == null || memberId == null) {
        Console.error("uso: emprestar <livro> <membro>")
        return
    }

    when (val result = service.borrow(bookId, memberId)) {
        is LibraryService.ActionResult.Ok -> Console.info(result.message)
        is LibraryService.ActionResult.Err -> Console.error(result.message)
    }
}

/**
 * Devolve um exemplar. Mesmo padrão do emprestar.
 */
private fun showReturn(service: LibraryService, command: Command) {
    val bookId = command.argument(0)?.toIntOrNull()
    val memberId = command.argument(1)?.toIntOrNull()
    if (bookId == null || memberId == null) {
        Console.error("uso: devolver <livro> <membro>")
        return
    }

    when (val result = service.returnBook(bookId, memberId)) {
        is LibraryService.ActionResult.Ok -> Console.info(result.message)
        is LibraryService.ActionResult.Err -> Console.error(result.message)
    }
}