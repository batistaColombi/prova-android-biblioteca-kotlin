package biblioteca.service

import biblioteca.data.Library
import biblioteca.model.Book
import biblioteca.model.Loan
import java.text.Normalizer
import java.time.LocalDate

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

    /**
     * Empréstimo e devolução (tarefa 3).
     * Prazo de 14 dias, no máximo 3 por membro, e quem está atrasado não empresta.
     * Devolve Ok/Err com a mensagem; quem imprime é o Main.
     */
    companion object {
        const val LOAN_DAYS = 14L
        const val MAX_LOANS_PER_MEMBER = 3
    }

    sealed class ActionResult {
        data class Ok(val message: String) : ActionResult()
        data class Err(val message: String) : ActionResult()
    }

    fun dueDate(loan: Loan): LocalDate = loan.borrowedAt.plusDays(LOAN_DAYS)

    fun isOverdue(loan: Loan, today: LocalDate = LocalDate.now()): Boolean =
        dueDate(loan).isBefore(today)

    fun activeLoansOf(memberId: Int): List<Loan> =
        library.loans.filter { it.memberId == memberId }

    fun borrow(bookId: Int, memberId: Int): ActionResult {
        val book = library.findBook(bookId)
            ?: return ActionResult.Err("livro $bookId não encontrado")
        val member = library.findMember(memberId)
            ?: return ActionResult.Err("membro $memberId não encontrado")

        if (availableCopies(bookId) <= 0) {
            return ActionResult.Err("\"${book.title}\" não tem exemplares livres")
        }

        val memberLoans = activeLoansOf(memberId)
        if (memberLoans.any { isOverdue(it) }) {
            return ActionResult.Err("${member.name} tem devolução atrasada e não pode emprestar")
        }
        if (memberLoans.size >= MAX_LOANS_PER_MEMBER) {
            return ActionResult.Err("${member.name} já tem $MAX_LOANS_PER_MEMBER empréstimos")
        }

        library.loans.add(Loan(bookId, memberId, LocalDate.now()))
        val until = LocalDate.now().plusDays(LOAN_DAYS)
        val livres = availableCopies(bookId)
        return ActionResult.Ok(
            "${member.name} pegou \"${book.title}\".\nDevolver até $until.\nRestam $livres exemplar(es) livre(s)."
        )
    }

    fun returnBook(bookId: Int, memberId: Int): ActionResult {
        val book = library.findBook(bookId)
            ?: return ActionResult.Err("livro $bookId não encontrado")
        val member = library.findMember(memberId)
            ?: return ActionResult.Err("membro $memberId não encontrado")

        val loan = library.loans.find { it.bookId == bookId && it.memberId == memberId }
            ?: return ActionResult.Err(
                "${member.name} não tem empréstimo ativo de \"${book.title}\""
            )

        library.loans.remove(loan)
        val livres = availableCopies(bookId)
        return ActionResult.Ok(
            "${member.name} devolveu \"${book.title}\".\nRestam $livres exemplar(es) livre(s)."
        )
    }

    /**
     * Empréstimos ativos do membro, com prazo e se está atrasado.
     * Reaproveita activeLoansOf, dueDate e isOverdue da tarefa 3.
     */
    data class MemberLoanView(
        val bookId: Int,
        val title: String,
        val borrowedAt: LocalDate,
        val dueDate: LocalDate,
        val overdue: Boolean,
    )

    sealed class MemberLoansResult {
        data class Ok(val memberName: String, val loans: List<MemberLoanView>) : MemberLoansResult()
        data class Err(val message: String) : MemberLoansResult()
    }

    fun loansOfMember(memberId: Int): MemberLoansResult {
        val member = library.findMember(memberId)
            ?: return MemberLoansResult.Err("membro $memberId não encontrado")

        val loans = activeLoansOf(memberId).map { toMemberLoanView(it) }
        return MemberLoansResult.Ok(member.name, loans)
    }

    /**
     * Relatório: todos os empréstimos atrasados (todos os membros).
     * Reaproveita isOverdue, dueDate e MemberLoanView.
     */
    data class OverdueLoanRow(
        val memberId: Int,
        val memberName: String,
        val loan: MemberLoanView,
    )

    fun overdueLoans(): List<OverdueLoanRow> =
        library.loans
            .filter { isOverdue(it) }
            .map { loan ->
                val member = library.findMember(loan.memberId)
                OverdueLoanRow(
                    memberId = loan.memberId,
                    memberName = member?.name ?: "(membro ${loan.memberId})",
                    loan = toMemberLoanView(loan),
                )
            }

    private fun toMemberLoanView(loan: Loan): MemberLoanView {
        val book = library.findBook(loan.bookId)
        return MemberLoanView(
            bookId = loan.bookId,
            title = book?.title ?: "(livro ${loan.bookId})",
            borrowedAt = loan.borrowedAt,
            dueDate = dueDate(loan),
            overdue = isOverdue(loan),
        )
    }
}