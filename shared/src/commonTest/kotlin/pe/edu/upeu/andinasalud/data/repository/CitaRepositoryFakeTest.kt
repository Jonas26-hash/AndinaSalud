package pe.edu.upeu.andinasalud.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import pe.edu.upeu.andinasalud.domain.model.EstadoCita
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class CitaRepositoryFakeTest {

    private val ancla = LocalDate.parse("2026-01-10")

    @Test
    fun `el seed contiene seis citas con la distribucion esperada`() = runTest {
        val repositorio = CitaRepositoryFake(ancla = ancla)

        val citas = repositorio.obtenerCitas()

        assertEquals(6, citas.size)
        assertEquals(3, citas.count { it.estado is EstadoCita.Programada })
        assertEquals(2, citas.count { it.estado is EstadoCita.Atendida })
        assertEquals(1, citas.count { it.estado is EstadoCita.Cancelada })
    }

    @Test
    fun `el seed construye las programadas en el futuro respecto del momento`() = runTest {
        val repositorio = CitaRepositoryFake(ancla = ancla)

        val programadas = repositorio.obtenerCitas().filter { it.estado is EstadoCita.Programada }

        assertNotNull(programadas.firstOrNull())
        programadas.forEach { cita ->
            assertTrue(cita.fecha > ancla)
        }
        assertEquals(ancla.plus(2, kotlinx.datetime.DateTimeUnit.DAY), programadas[0].fecha)
    }

    @Test
    fun `reconoce al paciente fijo del caso`() = runTest {
        val repositorio = CitaRepositoryFake(ancla = ancla)

        val paciente = repositorio.obtenerPaciente()

        assertEquals("P-0417", paciente.id)
        assertEquals("Lucía Quispe Mamani", paciente.nombre)
        assertEquals("70154823", paciente.documento)
        assertEquals("lucia.quispe@correo.pe", paciente.correo)
    }

    @Test
    fun `solicitar registra una cita nueva y aumenta el contador de programadas`() = runTest {
        val repositorio = CitaRepositoryFake(ancla = ancla)

        val nueva = repositorio.solicitar(
            especialidad = "Pediatría",
            sede = "Chosica",
            fecha = LocalDate.parse("2026-01-28"),
            hora = LocalTime(10, 30),
            motivo = "Vacunas del calendario anual"
        )

        assertEquals(7, nueva.id)
        assertIs<EstadoCita.Programada>(nueva.estado)
        assertEquals(7, repositorio.obtenerCitas().size)
        assertEquals(4, repositorio.contarCitasProgramadas("P-0417"))
    }

    @Test
    fun `solicitar resuelve el medico de la especialidad y la sede`() = runTest {
        val repositorio = CitaRepositoryFake(ancla = ancla)

        val nueva = repositorio.solicitar(
            especialidad = "Pediatría",
            sede = "Chaclacayo",
            fecha = LocalDate.parse("2026-01-28"),
            hora = LocalTime(10, 30),
            motivo = "Vacunas del calendario anual"
        )

        assertEquals("Dra. Carla Núñez", nueva.medico.nombre)
        assertEquals("Chaclacayo", nueva.sede.nombre)
    }

    @Test
    fun `solicitar falla cuando no hay medico para la especialidad en la sede`() = runTest {
        val repositorio = CitaRepositoryFake(ancla = ancla)

        assertFailsWith<IllegalArgumentException> {
            repositorio.solicitar(
                especialidad = "Cardiología",
                sede = "Ñaña",
                fecha = LocalDate.parse("2026-01-28"),
                hora = LocalTime(10, 30),
                motivo = "Vacunas del calendario anual"
            )
        }
    }

    @Test
    fun `cancelar cambia el estado en la fuente`() = runTest {
        val repositorio = CitaRepositoryFake(ancla = ancla)

        val cancelada = repositorio.cancelar(
            idCita = 1,
            motivo = "Viaje del paciente",
            canceladaPorPaciente = true
        )

        val consultada = repositorio.obtenerCitaPorId(1)
        assertNotNull(consultada)
        assertIs<EstadoCita.Cancelada>(consultada.estado)
        val estado = assertIs<EstadoCita.Cancelada>(cancelada.estado)
        assertEquals("Viaje del paciente", estado.motivo)
        assertEquals(2, repositorio.obtenerCitas().count {
            it.estado is EstadoCita.Cancelada
        })
    }

    @Test
    fun `cancelar una cita inexistente falla`() = runTest {
        val repositorio = CitaRepositoryFake(ancla = ancla)

        assertFailsWith<IllegalArgumentException> {
            repositorio.cancelar(idCita = 999, motivo = "X", canceladaPorPaciente = true)
        }
    }

    @Test
    fun `detecta una programada en el mismo horario`() = runTest {
        val repositorio = CitaRepositoryFake(ancla = ancla)

        assertEquals(
            true,
            repositorio.existeProgramadaEnHorario(
                pacienteId = "P-0417",
                fecha = LocalDate.parse("2026-01-12"),
                hora = LocalTime(9, 0)
            )
        )

        assertEquals(
            false,
            repositorio.existeProgramadaEnHorario(
                pacienteId = "P-0417",
                fecha = LocalDate.parse("2026-01-12"),
                hora = LocalTime(8, 0)
            )
        )
    }
}