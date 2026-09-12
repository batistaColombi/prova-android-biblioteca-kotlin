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
            "sair" -> {
                Console.info("Até mais.")
                return
            }

            // TODO (Tarefas 2 a 4): implementar os comandos novos aqui.
            "buscar", "emprestar", "devolver", "membro" ->
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
        },
    )
}
