package biblioteca.data

import biblioteca.model.Loan
import java.io.File
import java.time.LocalDate

class LoanStore(private val file: File = File("loans.csv")) {

    fun load(): List<Loan>? {
        if (!file.exists()) return null  // “não tem arquivo” ≠ “lista vazia”

        return file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val parts = line.split(",")
                if (parts.size != 3) return@mapNotNull null
                val bookId = parts[0].toIntOrNull() ?: return@mapNotNull null
                val memberId = parts[1].toIntOrNull() ?: return@mapNotNull null
                val date = runCatching { LocalDate.parse(parts[2]) }.getOrNull()
                    ?: return@mapNotNull null
                Loan(bookId, memberId, date)
            }
    }

    fun save(loans: List<Loan>) {
        val text = loans.joinToString("\n") { loan ->
            "${loan.bookId},${loan.memberId},${loan.borrowedAt}"
        }
        file.writeText(text)
    }
}