package pe.edu.upeu.andinasalud.presentation.citas

import pe.edu.upeu.andinasalud.domain.model.Cita
import pe.edu.upeu.andinasalud.domain.model.EstadoCita


data class CitaUi(
    val id: Long,
    val especialidad: String,
    val medicoNombre: String,
    val sedeNombre: String,
    val fechaTexto: String,
    val horaTexto: String,
    val recordatorioActivo: Boolean?,
    val estado: EstadoCita,
    val indicaciones: String?,
    val cancelacionMotivo: String?,
    val canceladaPorPaciente: Boolean?
) {

    val esProgramada: Boolean
        get() = estado is EstadoCita.Programada

    val esAtendida: Boolean
        get() = estado is EstadoCita.Atendida

    val esCancelada: Boolean
        get() = estado is EstadoCita.Cancelada
}

fun Cita.aUi(): CitaUi = CitaUi(
    id = id,
    especialidad = especialidad,
    medicoNombre = medico.nombre,
    sedeNombre = sede.nombre,
    fechaTexto = fecha.formatoDiaMesAnio(),
    horaTexto = hora.formatoHora(),
    recordatorioActivo = (estado as? EstadoCita.Programada)?.recordatorioActivo,
    estado = estado,
    indicaciones = (estado as? EstadoCita.Atendida)?.indicaciones,
    cancelacionMotivo = (estado as? EstadoCita.Cancelada)?.motivo,
    canceladaPorPaciente = (estado as? EstadoCita.Cancelada)?.canceladaPorPaciente
)


fun EstadoCita.etiqueta(): String = when (this) {
    is EstadoCita.Programada -> "Programada"
    is EstadoCita.Atendida -> "Atendida"
    is EstadoCita.Cancelada -> "Cancelada"
}