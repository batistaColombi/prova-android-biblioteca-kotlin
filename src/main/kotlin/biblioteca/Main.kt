package biblioteca

import biblioteca.cli.Command
import biblioteca.cli.Console
import biblioteca.data.Library
import biblioteca.data.LoanStore
import biblioteca.data.ReturnStore
import biblioteca.service.LibraryService
import biblioteca.service.LibraryService.Companion.MAX_LOANS_PER_MEMBER
import kotlin.text.Charsets

fun main() {
    System.setOut(java.io.PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(java.io.PrintStream(System.err, true, Charsets.UTF_8))

    val loanStore = LoanStore()
    val returnStore = ReturnStore()
    val loaded = loanStore.load()
    val library = Library(initialLoans = loaded)
    val service = LibraryService(library, loanStore, returnStore)

    if (loaded == null) {
        loanStore.save(library.loans)
    }

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
            "membro" -> showMember(service, command)
            "atrasados" -> showOverdue(service)
            "limite" -> showAtLimit(service)
            "sair" -> {
                Console.info("Até mais.")
                return
            }
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
            listOf("atrasados", "lista empréstimos atrasados de todos os membros"),
            listOf("limite", "lista membros no limite de empréstimos"),
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

/**
 * Mostra os empréstimos ativos do membro, com prazo e se está atrasado.
 * Os dados vêm do service; aqui só monto a tabela.
 */
private fun showMember(service: LibraryService, command: Command) {
    val memberId = command.argument(0)?.toIntOrNull()
    if (memberId == null) {
        Console.error("uso: membro <id>")
        return
    }

    when (val result = service.loansOfMember(memberId)) {
        is LibraryService.MemberLoansResult.Err -> {
            Console.error(result.message)
        }
        is LibraryService.MemberLoansResult.Ok -> {
            Console.title("Membro: ${result.memberName}")
            if (result.loans.isEmpty()) {
                Console.info("Nenhum empréstimo ativo.")
                return
            }
            Console.table(
                headers = listOf("livro", "título", "emprestado em", "devolver até", "situação"),
                rows = result.loans.map { loan ->
                    listOf(
                        loan.bookId.toString(),
                        loan.title,
                        loan.borrowedAt.toString(),
                        loan.dueDate.toString(),
                        if (loan.overdue) "atrasado" else "em dia",
                    )
                },
            )
        }
    }
}

/**
 * Mostra os empréstimos ativos do membro, com prazo e se está atrasado.
 * Os dados vêm do service; aqui só monto a tabela.
 */
private fun showOverdue(service: LibraryService) {
    val loans = service.overdueLoans()
    Console.title("Empréstimos atrasados")
    if (loans.isEmpty()) {
        Console.info("Nenhum empréstimo atrasado.")
        return
    }
    Console.table(
        headers = listOf("membro", "nome", "livro", "título", "emprestado em", "devolver até"),
        rows = loans.map { row ->
            listOf(
                row.memberId.toString(),
                row.memberName,
                row.loan.bookId.toString(),
                row.loan.title,
                row.loan.borrowedAt.toString(),
                row.loan.dueDate.toString(),
            )
        },
    )
}

/**
 * Mostra os membros que já atingiram o limite de empréstimos.
 * Os dados vêm do service; aqui só monto a tabela.
 */
private fun showAtLimit(service: LibraryService) {
    val rows = service.membersAtLoanLimit()
    Console.title("Membros no limite de empréstimos")
    if (rows.isEmpty()) {
        Console.info("Nenhum membro no limite de $MAX_LOANS_PER_MEMBER empréstimos.")
        return
    }
    Console.table(
        headers = listOf("membro", "nome", "empréstimos"),
        rows = rows.map { row ->
            listOf(
                row.memberId.toString(),
                row.memberName,
                row.loanCount.toString(),
            )
        },
    )
}