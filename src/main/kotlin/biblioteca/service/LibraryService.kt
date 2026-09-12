package biblioteca.service

import biblioteca.data.Library
import biblioteca.model.Book
import java.text.Normalizer

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

    /**
     * Busca por título, autor ou gênero.
     * Ignora maiúsculas/minúsculas e acentos (`solidao` acha `Solidão`).
     */
    fun search(term: String): List<Book> {
        val needle = normalize(term)
        if (needle.isEmpty()) return emptyList()

        return library.books. filter {
            normalize(it.title).contains(needle) ||
                    normalize(it.author).contains(needle) ||
                    normalize(it.genre).contains(needle)
        }
    }

    private fun normalize(text: String): String {
        val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
        return decomposed
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
    }
    // TODO (Tarefa 3): emprestar e devolver, com as regras do enunciado.

    // TODO (Tarefa 4): o que um membro tem em mãos, e o que está atrasado.
}
