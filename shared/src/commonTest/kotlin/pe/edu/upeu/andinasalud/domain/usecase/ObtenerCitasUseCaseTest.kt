package pe.edu.upeu.andinasalud.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasalud.fakes.CitasDePrueba
import pe.edu.upeu.andinasalud.fakes.FakeCitaRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


class ObtenerCitasUseCaseTest {

    @Test
    fun `ordena las citas de la mas proxima a la mas lejana`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-03-01"), LocalTime(9, 0)),
                CitasDePrueba.programada(2, LocalDate.parse("2026-01-20"), LocalTime(9, 0)),
                CitasDePrueba.programada(3, LocalDate.parse("2026-02-15"), LocalTime(9, 0))
            )
        )
        val caso = ObtenerCitasUseCase(repositorio)

        val citas = caso()

        assertEquals(listOf(2L, 3L, 1L), citas.map { it.id })
    }

    @Test
    fun `desempata las citas del mismo dia por la hora`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-20"), LocalTime(17, 0)),
                CitasDePrueba.programada(2, LocalDate.parse("2026-01-20"), LocalTime(8, 0))
            )
        )
        val caso = ObtenerCitasUseCase(repositorio)

        assertEquals(listOf(2L, 1L), caso().map { it.id })
    }

    @Test
    fun `busca una cita por id y devuelve null si no existe`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(CitasDePrueba.programada(1, LocalDate.parse("2026-01-20")))
        )
        val caso = ObtenerCitasUseCase(repositorio)

        assertEquals(1L, caso.porId(1)?.id)
        assertNull(caso.porId(999))
    }

    @Test
    fun `expone el paciente, las especialidades y las sedes`() = runTest {
        val repositorio = FakeCitaRepository(emptyList())
        val caso = ObtenerCitasUseCase(repositorio)

        assertEquals("P-0417", caso.paciente().id)
        assertTrue(caso.especialidades().size == 5)
        assertEquals(listOf("Ñaña", "Chosica", "Chaclacayo", "Santa Anita"), caso.sedes().map { it.nombre })
    }
}