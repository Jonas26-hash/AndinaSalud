package pe.edu.upeu.andinasalud.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasalud.domain.model.EstadoCita
import pe.edu.upeu.andinasalud.domain.model.aEpochMillis
import pe.edu.upeu.andinasalud.fakes.CitasDePrueba
import pe.edu.upeu.andinasalud.fakes.FakeCitaRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue


class CancelarCitaUseCaseTest {

    private val ahoraMillis = LocalDate.parse("2026-01-10").aEpochMillis(LocalTime(9, 0))

    @Test
    fun `cancela una cita programada con mas de veinticuatro horas`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-15"), LocalTime(9, 0))
            )
        )
        val caso = CancelarCitaUseCase(repositorio)

        val resultado = caso(idCita = 1, ahoraMillis = ahoraMillis)

        val cita = resultado.getOrThrow()
        val cancelada = assertIs<EstadoCita.Cancelada>(cita.estado)
        assertEquals(CancelarCitaUseCase.MOTIVO_POR_DEFECTO, cancelada.motivo)
        assertTrue(cancelada.canceladaPorPaciente)
    }

    @Test
    fun `RN-03 rechaza cancelar con menos de veinticuatro horas`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-11"), LocalTime(8, 0))
            )
        )
        val caso = CancelarCitaUseCase(repositorio)

        val excepcion = assertFailsWith<CancelacionInvalidaException> {
            caso(idCita = 1, ahoraMillis = ahoraMillis).getOrThrow()
        }

        assertTrue(excepcion.message.orEmpty().contains("24"))
    }

    @Test
    fun `RN-03 rechaza cancelar exactamente a veinticuatro horas`() = runTest {

        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-11"), LocalTime(9, 0))
            )
        )
        val caso = CancelarCitaUseCase(repositorio)

        val excepcion = assertFailsWith<CancelacionInvalidaException> {
            caso(idCita = 1, ahoraMillis = ahoraMillis).getOrThrow()
        }

        assertTrue(excepcion.message.orEmpty().contains("24"))
    }

    @Test
    fun `RN-03 acepta cancelar apenas pasa de veinticuatro horas`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-11"), LocalTime(9, 1))
            )
        )
        val caso = CancelarCitaUseCase(repositorio)

        assertTrue(caso(idCita = 1, ahoraMillis = ahoraMillis).isSuccess)
    }

    @Test
    fun `no cancela una cita atendida`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(CitasDePrueba.atendida(1, LocalDate.parse("2026-01-05")))
        )
        val caso = CancelarCitaUseCase(repositorio)

        val excepcion = assertFailsWith<CancelacionInvalidaException> {
            caso(idCita = 1, ahoraMillis = ahoraMillis).getOrThrow()
        }

        assertTrue(excepcion.message.orEmpty().contains("programada"))
    }

    @Test
    fun `no cancela una cita inexistente`() = runTest {
        val caso = CancelarCitaUseCase(FakeCitaRepository(emptyList()))

        val excepcion = assertFailsWith<CancelacionInvalidaException> {
            caso(idCita = 99, ahoraMillis = ahoraMillis).getOrThrow()
        }

        assertTrue(excepcion.message.orEmpty().contains("no existe"))
    }

    @Test
    fun `no cancela una cita ya cancelada`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(CitasDePrueba.cancelada(1, LocalDate.parse("2026-01-05")))
        )
        val caso = CancelarCitaUseCase(repositorio)

        val excepcion = assertFailsWith<CancelacionInvalidaException> {
            caso(idCita = 1, ahoraMillis = ahoraMillis).getOrThrow()
        }

        assertTrue(excepcion.message.orEmpty().contains("programada"))
    }

    @Test
    fun `un fallo del repositorio llega como resultado de fallo`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-15"), LocalTime(9, 0))
            )
        )
        repositorio.errorSimulado = RuntimeException("Servidor caido")
        val caso = CancelarCitaUseCase(repositorio)

        assertTrue(caso(idCita = 1, ahoraMillis = ahoraMillis).isFailure)
    }
}