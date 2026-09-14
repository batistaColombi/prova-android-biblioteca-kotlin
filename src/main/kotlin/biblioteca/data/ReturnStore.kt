package biblioteca.data

import java.io.File
import java.time.LocalDate

/** Só acrescenta linhas em returns.csv — histórico de devoluções. */
class ReturnStore(private val file: File = File("returns.csv")) {

    fun append(bookId: Int, memberId: Int, borrowedAt: LocalDate, returnedAt: LocalDate) {
        val line = "$bookId,$memberId,$borrowedAt,$returnedAt"
        file.appendText(if (file.exists() && file.length() > 0) "\n$line" else line)
    }
}
