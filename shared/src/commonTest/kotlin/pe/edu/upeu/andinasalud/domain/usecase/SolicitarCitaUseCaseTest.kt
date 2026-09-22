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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class SolicitarCitaUseCaseTest {

    private val ahoraMillis = LocalDate.parse("2026-01-10").aEpochMillis(LocalTime(0, 0))

    @Test
    fun `registra una cita futura cuando todo es valido`() = runTest {
        val repositorio = FakeCitaRepository(listaInicial = listOf(CitasDePrueba.programada(1, LocalDate.parse("2026-01-20"))))
        val caso = SolicitarCitaUseCase(repositorio)

        val resultado = caso(
            especialidad = "Medicina General",
            sede = "Ñaña",
            fecha = "2026-01-12",
            hora = "09:00",
            motivo = "Control general anual",
            ahoraMillis = ahoraMillis
        )

        val cita = resultado.getOrThrow()
        assertEquals(2, cita.id)
        assertEquals("Medicina General", cita.especialidad)
        assertIs<EstadoCita.Programada>(cita.estado)
        assertEquals(true, cita.estado.recordatorioActivo)
        assertTrue(cita.instanteEpochMillis > ahoraMillis)
    }

    @Test
    fun `RN-01 rechaza una fecha pasada`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "Medicina General",
                sede = "Ñaña",
                fecha = "2026-01-05",
                hora = "09:00",
                motivo = "Control general anual",
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.fecha)
    }

    @Test
    fun `RN-01 rechaza el instante actual o pasado del mismo dia`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "Medicina General",
                sede = "Ñaña",
                fecha = "2026-01-10",
                hora = "00:00",
                motivo = "Control general anual",
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.fecha)
    }

    @Test
    fun `RN-01 rechaza una cita un minuto antes del momento actual`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "Medicina General",
                sede = "Ñaña",
                fecha = "2026-01-09",
                hora = "23:59",
                motivo = "Control general anual",
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.fecha)
    }

    @Test
    fun `RN-01 acepta una cita un minuto despues del momento actual`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val cita = caso(
            especialidad = "Medicina General",
            sede = "Ñaña",
            fecha = "2026-01-10",
            hora = "00:01",
            motivo = "Control general anual",
            ahoraMillis = ahoraMillis
        ).getOrThrow()

        assertTrue(cita.instanteEpochMillis > ahoraMillis)
    }

    @Test
    fun `RN-02 permite la tercera programada cuando hay dos`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-20")),
                CitasDePrueba.programada(2, LocalDate.parse("2026-01-21"))
            )
        )
        val caso = SolicitarCitaUseCase(repositorio)

        val cita = caso(
            especialidad = "Medicina General",
            sede = "Ñaña",
            fecha = "2026-01-22",
            hora = "09:00",
            motivo = "Control general anual",
            ahoraMillis = ahoraMillis
        ).getOrThrow()

        assertEquals(3L, cita.id)
        assertIs<EstadoCita.Programada>(cita.estado)
        assertEquals(3, repositorio.registros.count { it.estado is EstadoCita.Programada })
    }

    @Test
    fun `RN-02 limita a tres citas programadas simultaneas`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-20")),
                CitasDePrueba.programada(2, LocalDate.parse("2026-01-21")),
                CitasDePrueba.programada(3, LocalDate.parse("2026-01-22"))
            )
        )
        val caso = SolicitarCitaUseCase(repositorio)

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "Medicina General",
                sede = "Ñaña",
                fecha = "2026-01-23",
                hora = "09:00",
                motivo = "Control general anual",
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.general)
    }

    @Test
    fun `RN-04 rechaza un motivo menor a diez caracteres`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "Medicina General",
                sede = "Ñaña",
                fecha = "2026-01-12",
                hora = "09:00",
                motivo = "corto",
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.motivo)
    }

    @Test
    fun `RN-04 rechaza un motivo de nueve caracteres`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "Medicina General",
                sede = "Ñaña",
                fecha = "2026-01-12",
                hora = "09:00",
                motivo = "x".repeat(9),
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.motivo)
    }

    @Test
    fun `RN-04 acepta un motivo de exactamente diez caracteres`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val resultado = caso(
            especialidad = "Medicina General",
            sede = "Ñaña",
            fecha = "2026-01-12",
            hora = "09:00",
            motivo = "x".repeat(10),
            ahoraMillis = ahoraMillis
        )

        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `RN-04 rechaza un motivo mayor a doscientos caracteres`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "Medicina General",
                sede = "Ñaña",
                fecha = "2026-01-12",
                hora = "09:00",
                motivo = "x".repeat(201),
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.motivo)
    }

    @Test
    fun `RN-04 acepta un motivo de exactamente doscientos caracteres`() = runTest {
        val repositorio = FakeCitaRepository(emptyList())
        val caso = SolicitarCitaUseCase(repositorio)

        val resultado = caso(
            especialidad = "Medicina General",
            sede = "Ñaña",
            fecha = "2026-01-12",
            hora = "09:00",
            motivo = "x".repeat(200),
            ahoraMillis = ahoraMillis
        )

        assertTrue(resultado.isSuccess)
    }

    @Test
    fun `RN-05 rechaza repetir programada en la misma fecha y hora`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-12"), LocalTime(9, 0))
            )
        )
        val caso = SolicitarCitaUseCase(repositorio)

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "Medicina General",
                sede = "Ñaña",
                fecha = "2026-01-12",
                hora = "09:00",
                motivo = "Control general anual",
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.general)
    }

    @Test
    fun `RN-05 permite otra hora del mismo dia`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-12"), LocalTime(9, 0))
            )
        )
        val caso = SolicitarCitaUseCase(repositorio)

        val cita = caso(
            especialidad = "Medicina General",
            sede = "Ñaña",
            fecha = "2026-01-12",
            hora = "10:00",
            motivo = "Control general anual",
            ahoraMillis = ahoraMillis
        ).getOrThrow()

        assertEquals(LocalTime(10, 0), cita.hora)
    }

    @Test
    fun `RN-05 permite una fecha diferente`() = runTest {
        val repositorio = FakeCitaRepository(
            listaInicial = listOf(
                CitasDePrueba.programada(1, LocalDate.parse("2026-01-12"), LocalTime(9, 0))
            )
        )
        val caso = SolicitarCitaUseCase(repositorio)

        val cita = caso(
            especialidad = "Medicina General",
            sede = "Ñaña",
            fecha = "2026-01-13",
            hora = "09:00",
            motivo = "Control general anual",
            ahoraMillis = ahoraMillis
        ).getOrThrow()

        assertEquals(LocalDate.parse("2026-01-13"), cita.fecha)
    }

    @Test
    fun `pide especialidad y sede antes de mirar el resto`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "",
                sede = "",
                fecha = "2026-01-12",
                hora = "09:00",
                motivo = "Control general anual",
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.especialidad)
        assertNotNull(excepcion.errores.sede)
    }

    @Test
    fun `rechaza una fecha mal formateada`() = runTest {
        val caso = SolicitarCitaUseCase(FakeCitaRepository(emptyList()))

        val excepcion = assertFailsWith<SolicitudInvalidaException> {
            caso(
                especialidad = "Medicina General",
                sede = "Ñaña",
                fecha = "12/01/2026",
                hora = "09:00",
                motivo = "Control general anual",
                ahoraMillis = ahoraMillis
            ).getOrThrow()
        }

        assertNotNull(excepcion.errores.fecha)
    }

    @Test
    fun `un fallo del repositorio llega como resultado sin validaciones falsas`() = runTest {
        val repositorio = FakeCitaRepository(emptyList())
        repositorio.errorSimulado = RuntimeException("Servidor caido")
        val caso = SolicitarCitaUseCase(repositorio)

        val resultado = caso(
            especialidad = "Medicina General",
            sede = "Ñaña",
            fecha = "2026-01-12",
            hora = "09:00",
            motivo = "Control general anual",
            ahoraMillis = ahoraMillis
        )

        assertTrue(resultado.isFailure)
    }
}