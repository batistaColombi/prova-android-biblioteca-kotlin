package biblioteca.service

import biblioteca.data.Library
import biblioteca.model.Book

/**
 * Onde moram as regras da biblioteca.
 *
 * Nada aqui dentro imprime na tela nem lê do teclado
 * Esta classe recebe perguntas e devolve dados. Quem conversa com o usuário é a camada de `cli`.
 */
class LibraryService(private val library: Library) {

    /**
     * O acervo inteiro, na ordem em que está cadastrado.
     */
    fun catalog(): List<Book> = library.books

    /**
     * Quantos exemplares deste título estão livres agora.
     *
     * Total de cópias menos os empréstimos em aberto do livro.
     */
    fun availableCopies(bookId: Int): Int {
        val book = requireNotNull(library.findBook(bookId)) { "Livro $bookId não encontrado." }
        val activeLoans = library.loans.count { it.bookId == bookId }
        return (book.copies - activeLoans).coerceAtLeast(0)
    }

    // TODO (Tarefa 2): busca por título, autor ou gênero.

    // TODO (Tarefa 3): emprestar e devolver, com as regras do enunciado.

    // TODO (Tarefa 4): o que um membro tem em mãos, e o que está atrasado.
}
