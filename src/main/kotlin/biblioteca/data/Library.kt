package biblioteca.data

import biblioteca.model.Book
import biblioteca.model.Loan
import biblioteca.model.Member
import java.time.LocalDate

/**
 * O acervo da biblioteca, em memória.
 *
 * Faz o papel de banco de dados: guarda os dados e não conhece nenhuma regra.
 * [initialLoans] null → seed de demonstração; lista (mesmo vazia) → veio do arquivo.
 */
class Library(
    initialLoans: List<Loan>? = null,
) {
    val books: List<Book> = listOf(
        Book(1, "Duna", "Frank Herbert", "Ficção Científica", copies = 3),
        Book(2, "Ensaio sobre a Cegueira", "José Saramago", "Romance", copies = 2),
        Book(3, "Memórias Póstumas de Brás Cubas", "Machado de Assis", "Romance", copies = 4),
        Book(4, "Cem Anos de Solidão", "Gabriel García Márquez", "Realismo Mágico", copies = 2),
        Book(5, "O Hobbit", "J. R. R. Tolkien", "Fantasia", copies = 3),
        Book(6, "Neuromancer", "William Gibson", "Ficção Científica", copies = 1),
        Book(7, "A Revolução dos Bichos", "George Orwell", "Fábula", copies = 2),
        Book(8, "O Cortiço", "Aluísio Azevedo", "Naturalismo", copies = 1),
        Book(9, "Vidas Secas", "Graciliano Ramos", "Romance", copies = 2),
        Book(10, "Fahrenheit 451", "Ray Bradbury", "Ficção Científica", copies = 1),
        Book(11, "O Nome do Vento", "Patrick Rothfuss", "Fantasia", copies = 2),
        Book(12, "Ranger: A Ordem dos Arqueiros", "John Flanagan", "Fantasia", copies = 3),
    )

    val members: List<Member> = listOf(
        Member(1, "Ana Prado"),
        Member(2, "Bruno Sales"),
        Member(3, "Carla Nunes"),
        Member(4, "Diego Farias"),
    )

    // Se o Main passou uma lista (veio do arquivo), usa ela.
    // Se passou null (primeira vez / testes), usa o seed de sempre.
    val loans: MutableList<Loan> = (
        initialLoans ?: listOf(
            Loan(bookId = 1, memberId = 1, borrowedAt = LocalDate.now().minusDays(3)),
            Loan(bookId = 5, memberId = 1, borrowedAt = LocalDate.now().minusDays(20)),
            Loan(bookId = 6, memberId = 2, borrowedAt = LocalDate.now().minusDays(5)),
            Loan(bookId = 1, memberId = 3, borrowedAt = LocalDate.now().minusDays(10)),
        )
    ).toMutableList()

    fun findBook(id: Int): Book? = books.find { it.id == id }

    fun findMember(id: Int): Member? = members.find { it.id == id }
}
