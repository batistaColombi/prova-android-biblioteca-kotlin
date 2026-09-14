package biblioteca

import biblioteca.data.LoanStore
import biblioteca.model.Loan
import java.io.File
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoanStoreTest {

    @Test
    fun `save depois load devolve a mesma lista`() {
        val file = File.createTempFile("loans-test-", ".csv")
        file.deleteOnExit()
        val store = LoanStore(file)

        val originais = listOf(
            Loan(1, 2, LocalDate.of(2026, 9, 1)),
            Loan(5, 1, LocalDate.of(2026, 8, 20)),
        )
        store.save(originais)

        assertEquals(originais, store.load())
    }

    @Test
    fun `load sem arquivo devolve null`() {
        val file = File.createTempFile("loans-missing-", ".csv")
        file.delete() // garante que não existe

        assertNull(LoanStore(file).load())
    }
}
