package pe.edu.upeu.andinasalud.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class Cita(
    val id: Long,
    val paciente: Paciente,
    val especialidad: String,
    val medico: Medico,
    val sede: Sede,
    val fecha: LocalDate,
    val hora: LocalTime,
    val estado: EstadoCita
) {

    init {
        require(especialidad.isNotBlank()) { "La especialidad de la cita no puede estar vacía" }
    }

    val instanteEpochMillis: Long
        get() = fecha.aEpochMillis(hora)
}