package biblioteca

import biblioteca.data.Library
import biblioteca.service.LibraryService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LibraryServiceTest {

    @Test
    fun `Duna tem 1 exemplar livre`() {
        val service = LibraryService(Library())

        assertEquals(1, service.availableCopies(1))
    }

    @Test
    fun `busca ignora acento e caixa`() {
        val service = LibraryService(Library())

        assertEquals(listOf(4), service.search("solidao").map { it.id })
        assertEquals(listOf(2), service.search("SARAMAGO").map { it.id })
        assertEquals(emptyList(), service.search("xyzinexistente"))
    }

    @Test
    fun `Ana com atraso nao empresta`() {
        val service = LibraryService(Library())

        val resultado = service.borrow(7, 1)

        assertTrue(resultado is LibraryService.ActionResult.Err)
        assertTrue(
            (resultado as LibraryService.ActionResult.Err)
                .message
                .contains("atrasada")
        )
    }

    /** Diego (4) não tem atraso no seed — o bloqueio é só pelo teto de 3. */
    @Test
    fun `membro no limite sem atraso nao empresta o quarto`() {
        val service = LibraryService(Library())

        assertTrue(service.borrow(7, 4) is LibraryService.ActionResult.Ok)
        assertTrue(service.borrow(8, 4) is LibraryService.ActionResult.Ok)
        assertTrue(service.borrow(9, 4) is LibraryService.ActionResult.Ok)

        val quarto = service.borrow(10, 4)

        assertTrue(quarto is LibraryService.ActionResult.Err)
        val mensagem = (quarto as LibraryService.ActionResult.Err).message
        assertTrue(mensagem.contains("3 empréstimos"))
        assertFalse(mensagem.contains("atrasada"))
    }

    @Test
    fun `devolver aumenta exemplares livres`() {
        val service = LibraryService(Library())
        val antes = service.availableCopies(1)

        val resultado = service.returnBook(1, 3)

        assertTrue(resultado is LibraryService.ActionResult.Ok)
        assertEquals(antes + 1, service.availableCopies(1))
    }

    @Test
    fun `devolver inexistente falha`() {
        val service = LibraryService(Library())

        // Diego (4) não tem empréstimo de Duna (1) no seed
        val resultado = service.returnBook(1, 4)

        assertTrue(resultado is LibraryService.ActionResult.Err)
        assertTrue(
            (resultado as LibraryService.ActionResult.Err)
                .message
                .contains("não tem empréstimo ativo")
        )
    }
}
