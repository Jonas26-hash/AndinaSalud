package pe.edu.upeu.andinasalud.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.andinasalud.fakes.CitasDePrueba
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class CitaTest {

    @Test
    fun `instanteEpochMillis combina fecha y hora en un unico instante`() {
        val cita = CitasDePrueba.programada(
            id = 1,
            fecha = LocalDate.parse("2026-01-12"),
            hora = LocalTime(9, 0)
        )

        val esperado = LocalDate.parse("2026-01-12").aEpochMillis(LocalTime(9, 0))

        assertEquals(esperado, cita.instanteEpochMillis)
    }

    @Test
    fun `el instante respeta las horas del dia`() {
        val manana = CitasDePrueba.programada(
            id = 1,
            fecha = LocalDate.parse("2026-01-12"),
            hora = LocalTime(9, 0)
        )
        val tarde = CitasDePrueba.programada(
            id = 2,
            fecha = LocalDate.parse("2026-01-12"),
            hora = LocalTime(16, 30)
        )

        assert(tarde.instanteEpochMillis > manana.instanteEpochMillis)
    }

    @Test
    fun `rechaza una especialidad en blanco`() {
        assertFailsWith<IllegalArgumentException> {
            CitasDePrueba.programada(
                id = 1,
                fecha = LocalDate.parse("2026-01-12"),
                hora = LocalTime(9, 0)
            ).copy(especialidad = " ")
        }
    }
}


class EstadoCitaTest {

    @Test
    fun `Programada traslada el recordatorio`() {
        val estado = EstadoCita.Programada(recordatorioActivo = true)
        assertEquals(true, estado.recordatorioActivo)
    }

    @Test
    fun `Atendida traslada las indicaciones`() {
        val estado = EstadoCita.Atendida(indicaciones = "Control en tres meses")
        assertEquals("Control en tres meses", estado.indicaciones)
    }

    @Test
    fun `Cancelada traslada el motivo y quien la cancelo`() {
        val estado = EstadoCita.Cancelada(
            motivo = "Viaje del paciente",
            canceladaPorPaciente = true
        )

        assertEquals("Viaje del paciente", estado.motivo)
        assertEquals(true, estado.canceladaPorPaciente)
        assertEquals(false, estado.copy(canceladaPorPaciente = false).canceladaPorPaciente)
    }
}