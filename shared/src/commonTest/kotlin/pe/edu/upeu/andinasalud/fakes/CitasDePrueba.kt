package pe.edu.upeu.andinasalud.fakes

import pe.edu.upeu.andinasalud.data.local.CitasSimuladas
import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.EstadoCita
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime


object CitasDePrueba {

    fun programada(
        id: Long,
        fecha: LocalDate,
        hora: LocalTime = LocalTime(9, 0),
        recordatorioActivo: Boolean = true
    ): Cita = cita(id, fecha, hora, EstadoCita.Programada(recordatorioActivo))

    fun atendida(
        id: Long,
        fecha: LocalDate,
        hora: LocalTime = LocalTime(9, 0)
    ): Cita = cita(id, fecha, hora, EstadoCita.Atendida(indicaciones = "Control en tres meses"))

    fun cancelada(
        id: Long,
        fecha: LocalDate,
        hora: LocalTime = LocalTime(9, 0)
    ): Cita = cita(
        id, fecha, hora,
        EstadoCita.Cancelada(motivo = "Viaje del paciente", canceladaPorPaciente = true)
    )

    private fun cita(
        id: Long,
        fecha: LocalDate,
        hora: LocalTime,
        estado: EstadoCita
    ): Cita = Cita(
        id = id,
        paciente = CitasSimuladas.paciente,
        especialidad = "Medicina General",
        medico = CitasSimuladas.medicos[0],
        sede = CitasSimuladas.sedes[0],
        fecha = fecha,
        hora = hora,
        estado = estado
    )
}